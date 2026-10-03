package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BirdAdmirer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.w.WingShredder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CantStayAway.class, GrizzlyBears.class, HillGiant.class,
        BirdAdmirer.class, WingShredder.class, TurnToFrog.class})
class CantStayAwayTest extends BaseCardTest {

    @Test
    void returnsCreatureWithManaValueThreeOrLess() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CantStayAway()));
        addCantStayAwayMana();

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetCreatureWithManaValueGreaterThanThree() {
        Card creature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CantStayAway()));
        addCantStayAwayMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnedCreatureIsExiledIfItDies() {
        Card creature = new GrizzlyBears();
        castCantStayAway(creature);
        Permanent returned = findCreaturePermanent(player1, "Grizzly Bears");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returned));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnedCreatureCanReturnToHand() {
        Card creature = new GrizzlyBears();
        castCantStayAway(creature);
        Permanent returned = findCreaturePermanent(player1, "Grizzly Bears");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, returned));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    void flashbackReturnsCreatureAndExilesCantStayAway() {
        CantStayAway spell = new CantStayAway();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(spell, creature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    void returnsCreatureWithManaValueExactlyThree() {
        Card creature = new BirdAdmirer();
        castCantStayAway(creature);

        harness.assertOnBattlefield(player1, "Bird Admirer");
        harness.assertNotInGraveyard(player1, "Bird Admirer");
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new CantStayAway()));
        addCantStayAwayMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreatureCard() {
        Card noncreature = new CantStayAway();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new CantStayAway()));
        addCantStayAwayMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReturnTargetThatLeftTheGraveyardBeforeResolution() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CantStayAway()));
        addCantStayAwayMana();
        harness.castSorcery(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Can't Stay Away");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    void losingAllAbilitiesRemovesTheGrantedExileReplacement() {
        Card creature = new GrizzlyBears();
        castCantStayAway(creature);
        Permanent returned = findCreaturePermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, returned.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returned));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    void exileReplacementDoesNotFollowCreatureAfterItLeavesAndReenters() {
        Card creature = new GrizzlyBears();
        castCantStayAway(creature);
        Permanent returned = findCreaturePermanent(player1, "Grizzly Bears");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, returned));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent recast = findCreaturePermanent(player1, "Grizzly Bears");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, recast));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    void flashbackSpellIsExiledWhenItsTargetBecomesIllegal() {
        CantStayAway spell = new CantStayAway();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(spell, creature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFlashback(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Can't Stay Away");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    private void addCantStayAwayMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void castCantStayAway(Card creature) {
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CantStayAway()));
        addCantStayAwayMana();
        harness.castAndResolveSorcery(player1, 0, creature.getId());
    }

    private Permanent findCreaturePermanent(Player player, String name) {
        return harness.getGameQueryService().findPermanentById(gd, harness.getPermanentId(player, name));
    }
}
