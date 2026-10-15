package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AinokSurvivalist;
import com.github.laxika.magicalvibes.cards.e.ExitSpecialist;
import com.github.laxika.magicalvibes.cards.r.RattleclawMystic;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QarsiDeceiver.class, RattleclawMystic.class, AinokSurvivalist.class, ExitSpecialist.class})
class QarsiDeceiverTest extends BaseCardTest {

    @Test
    void restrictedManaCannotCastNormalSpell() {
        addCreatureReady(player1, new QarsiDeceiver());
        harness.setHand(player1, List.of(new RattleclawMystic()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedManaCastsFaceDownSpellAndTurnsItFaceUp() {
        addCreatureReady(player1, new QarsiDeceiver());
        addCreatureReady(player1, new QarsiDeceiver());
        harness.setHand(player1, List.of(new RattleclawMystic()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mystic = findPermanent(player1, "Rattleclaw Mystic");
        assertThat(mystic.isFaceDown()).isTrue();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mystic));

        assertThat(mystic.isFaceDown()).isFalse();
    }

    @Test
    void restrictedManaPaysManifestedCreatureManaCost() {
        addCreatureReady(player1, new QarsiDeceiver());
        Permanent manifested = addCreatureReady(player1, new QarsiDeceiver());
        manifested.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        manifested.setManifested(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, 1);

        assertThat(manifested.isFaceDown()).isFalse();
    }

    @Test
    void restrictedManaPaysMegamorphCost() {
        addCreatureReady(player1, new QarsiDeceiver());
        Permanent survivalist = addCreatureReady(player1, new AinokSurvivalist());
        survivalist.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, 1);

        assertThat(survivalist.isFaceDown()).isFalse();
    }

    @Test
    void restrictedManaCannotPayCloakedCreatureManaCost() {
        addCreatureReady(player1, new QarsiDeceiver());
        Permanent cloaked = addCreatureReady(player1, new QarsiDeceiver());
        cloaked.setFaceDownAsCloaked();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cloaked.isFaceDown()).isTrue();
    }

    @Test
    void restrictedManaCannotPayDisguiseCost() {
        addCreatureReady(player1, new QarsiDeceiver());
        Permanent disguised = addCreatureReady(player1, new ExitSpecialist());
        disguised.setFaceDownAsDisguised();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(disguised.isFaceDown()).isTrue();
    }
}
