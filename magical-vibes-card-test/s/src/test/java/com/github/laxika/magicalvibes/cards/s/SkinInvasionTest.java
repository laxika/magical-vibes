package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.Reclaim;
import com.github.laxika.magicalvibes.cards.v.VesselOfParamnesia;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkinInvasion.class, SkinShedder.class, FountainOfYouth.class, GrizzlyBears.class, LightningBolt.class,
        DevilthornFox.class, FieryTemper.class, Reclaim.class, VesselOfParamnesia.class})
class SkinInvasionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature must attack each combat if able")
    void enchantedCreatureMustAttack() {
        Permanent creature = addEnchantedCreature();
        creature.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Returns transformed when the enchanted creature dies")
    void returnsTransformedWhenEnchantedCreatureDies() {
        Permanent creature = addEnchantedCreature();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SkinInvasion)
                .findFirst()
                .orElseThrow();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(aura.getOriginalCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCard()).isInstanceOf(SkinShedder.class);
        assertThat(returned.isTransformed()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(aura.getOriginalCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SkinInvasion()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({SkinInvasion.class, SkinShedder.class, DevilthornFox.class})
    void tappedCreatureDoesNotHaveToAttack() {
        Permanent creature = addEnchantedFox();
        creature.setSummoningSick(false);
        creature.tap();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @CardUsed({SkinInvasion.class, SkinShedder.class, DevilthornFox.class})
    void summoningSickCreatureDoesNotHaveToAttack() {
        Permanent creature = addEnchantedFox();
        creature.setSummoningSick(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @CardUsed({SkinInvasion.class, SkinShedder.class, DevilthornFox.class, FieryTemper.class})
    void returnsUnderAuraControllersControlInsteadOfOwnersControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        SkinInvasion card = new SkinInvasion();
        card.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, card);
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(card.getId())
                        && permanent.getCard() instanceof SkinShedder && permanent.isTransformed());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getId().equals(card.getId()));
        harness.assertNotInGraveyard(player1, "Skin Invasion");
    }

    @Test
    @CardUsed({SkinInvasion.class, SkinShedder.class, DevilthornFox.class, FieryTemper.class,
            Reclaim.class, VesselOfParamnesia.class})
    void doesNotReturnAuraThatLeftGraveyardAndWasMilledBack() {
        Permanent creature = addEnchantedFox();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SkinInvasion)
                .findFirst().orElseThrow();
        harness.addToBattlefield(player1, new VesselOfParamnesia());
        harness.setLibrary(player1, List.of(new DevilthornFox(), new DevilthornFox(), new DevilthornFox()));
        harness.setHand(player1, List.of(new FieryTemper(), new Reclaim()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertInGraveyard(player1, "Skin Invasion");
        harness.castAndResolveInstant(player1, 0, aura.getOriginalCard().getId());
        harness.assertNotInGraveyard(player1, "Skin Invasion");
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Skin Invasion");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skin Invasion");
        harness.assertNotOnBattlefield(player1, "Skin Shedder");
    }

    private Permanent addEnchantedFox() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.setHand(player1, List.of(new SkinInvasion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return creature;
    }

    private Permanent addEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SkinInvasion());
        aura.setAttachedTo(creature.getId());
        return creature;
    }
}
