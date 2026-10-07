package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StratusWalk.class, TimberpackWolf.class, ScrapskinDrake.class, Disperse.class})
class StratusWalkTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Stratus Walk attaches it to the target creature and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent wolfPerm = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());
        wolfPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new StratusWalk()));
        harness.setLibrary(player1, List.of(new TimberpackWolf()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, wolfPerm.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Stratus Walk")
                        && p.isAttached()
                        && p.getAttachedTo().equals(wolfPerm.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent wolfPerm = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());
        wolfPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new StratusWalk());
        auraPerm.setAttachedTo(wolfPerm.getId());

        assertThat(gqs.hasKeyword(gd, wolfPerm, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can block a creature with flying")
    void enchantedCreatureCanBlockFlyer() {
        Permanent wolfPerm = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());
        wolfPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player2, new StratusWalk());
        auraPerm.setAttachedTo(wolfPerm.getId());

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new ScrapskinDrake());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wolfPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature cannot block a creature without flying")
    void enchantedCreatureCannotBlockNonFlyer() {
        Permanent wolfPerm = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());
        wolfPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player2, new StratusWalk());
        auraPerm.setAttachedTo(wolfPerm.getId());

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Unenchanted creature can still block a nonflyer once the Aura leaves")
    void restrictionEndsWhenAuraLeaves() {
        Permanent wolfPerm = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());
        wolfPerm.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new StratusWalk());
        aura.setAttachedTo(wolfPerm.getId());
        assertThat(gqs.hasKeyword(gd, wolfPerm, Keyword.FLYING)).isTrue();

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aura);
        assertThat(gqs.hasKeyword(gd, wolfPerm, Keyword.FLYING)).isFalse();

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wolfPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanting an opponent's creature draws for the Aura's controller")
    void enchantingOpponentsCreatureDrawsForAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());
        harness.setHand(player1, List.of(new StratusWalk()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new TimberpackWolf()));
        harness.setLibrary(player2, List.of(new ScrapskinDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof StratusWalk
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("An Aura with a vanished target does not enter or draw a card")
    void vanishedTargetDoesNotDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());
        harness.setHand(player1, List.of(new StratusWalk(), new Disperse()));
        harness.setLibrary(player1, List.of(new ScrapskinDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Stratus Walk");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof StratusWalk);
    }

    @Test
    @DisplayName("The enter trigger still draws if the Aura leaves before it resolves")
    void drawTriggerSurvivesAuraLeaving() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());
        harness.setHand(player1, List.of(new StratusWalk(), new Disperse()));
        harness.setLibrary(player1, List.of(new ScrapskinDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof StratusWalk)
                .findFirst().orElseThrow();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.castInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .anyMatch(c -> c instanceof StratusWalk)
                .anyMatch(c -> c instanceof ScrapskinDrake);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }
}
