package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfuseWithVitality.class, GrizzlyBears.class, Forest.class, DoomBlade.class})
class InfuseWithVitalityTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains deathtouch and you gain 2 life")
    void grantsDeathtouchAndLife() {
        Permanent creature = addCreature(player1);

        castOn(creature);

        assertThat(creature.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("The targeted creature returns tapped under its owner's control when it dies")
    void returnsTappedUnderOwnersControl() {
        Permanent creature = addCreature(player2);
        Card creatureCard = creature.getCard();

        castOn(creature);
        destroy(player1, creature);
        harness.passBothPriorities(); // resolve the granted death trigger

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                p -> p.getCard().getId().equals(creatureCard.getId()) && p.isTapped());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("The granted abilities wear off at end of turn")
    void grantsWearOffAtEndOfTurn() {
        Permanent creature = addCreature(player1);
        Card creatureCard = creature.getCard();

        castOn(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        destroy(player2, creature);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = addLand(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new InfuseWithVitality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No life is gained if the only target dies before resolution")
    void illegalTargetPreventsLifeGain() {
        Permanent creature = addCreature(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new InfuseWithVitality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, creature.getId());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The returned creature has neither granted ability and stays dead on its second death")
    void returnedCreatureDoesNotKeepGrantedAbilities() {
        Permanent creature = addCreature(player1);
        Card card = creature.getCard();
        castOn(creature);
        destroy(player2, creature);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(card.getId())).findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        destroy(player2, returned);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed({Humble.class})
    @DisplayName("An ability granted after Humble still triggers when the creature dies")
    void deathAbilityGrantedAfterAbilityRemovalStillTriggers() {
        Permanent creature = addCreature(player1);
        Card card = creature.getCard();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        castOn(creature);
        destroy(player2, creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                p -> p.getCard().getId().equals(card.getId()) && p.isTapped());
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature controlled by someone other than its owner returns to its owner")
    void stolenCreatureReturnsToOwner() {
        Card card = new GrizzlyBears();
        card.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);

        castOn(creature);
        destroy(player2, creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                p -> p.getCard().getId().equals(card.getId()) && p.isTapped());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(card.getId()));
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private void castOn(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new InfuseWithVitality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void destroy(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }
}
