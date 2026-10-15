package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlayersPlate.class, Deathmark.class, EliteVanguard.class, GrizzlyBears.class})
class SlayersPlateTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusFourPlusTwo() {
        Permanent creature = addCreature(player1, new EliteVanguard());
        Permanent plate = addPlate(player1);
        plate.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void createsSpiritWhenEquippedHumanDies() {
        Permanent creature = addCreature(player1, new EliteVanguard());
        Permanent plate = addPlate(player1);
        plate.setAttachedTo(creature.getId());

        killCreature(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.SPIRIT)
                        && p.getCard().getKeywords().contains(Keyword.FLYING)
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);
    }

    @Test
    void doesNotCreateSpiritWhenEquippedNonHumanDies() {
        Permanent creature = addCreature(player1, new GrizzlyBears());
        Permanent plate = addPlate(player1);
        plate.setAttachedTo(creature.getId());

        killCreature(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.SPIRIT));
    }

    @Test
    void doesNotCreateSpiritWhenHumanDiesUnequipped() {
        Permanent creature = addCreature(player1, new EliteVanguard());
        addPlate(player1);

        killCreature(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.SPIRIT));
    }

    @Test
    void equipPaysThreeManaAndMovesBoostToNewCreature() {
        Permanent first = addCreature(player1, new EliteVanguard());
        Permanent second = addCreature(player1, new EliteVanguard());
        Permanent plate = addPlate(player1);
        plate.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void spiritBelongsToEquipmentControllerWhenOpponentsHumanDies() {
        Permanent creature = addCreature(player2, new EliteVanguard());
        Permanent plate = addPlate(player1);
        plate.setAttachedTo(creature.getId());

        killCreature(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void removingPlateAfterHumanDiesDoesNotStopSpiritCreation() {
        Permanent creature = addCreature(player1, new EliteVanguard());
        Permanent plate = addPlate(player1);
        plate.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player2, 0, creature.getId());
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(plate);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .hasSize(1);
    }

    private Permanent addPlate(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SlayersPlate());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player2, 0, creature.getId());
        harness.passBothPriorities();
    }
}
