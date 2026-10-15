package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.j.JinnieFayJetmirsSecond;
import com.github.laxika.magicalvibes.cards.v.Vigor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EzurisPredation.class, HillGiant.class, AirElemental.class, DoublingSeason.class, Vigor.class, JinnieFayJetmirsSecond.class})
class EzurisPredationTest extends BaseCardTest {

    @Test
    void createsOneBeastForEachOpposingCreatureAndEachFightsADifferentCreature() {
        addCreatureReady(player2, new HillGiant());
        addCreatureReady(player2, new AirElemental());

        castEzurisPredation();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Air Elemental");

        List<Permanent> beasts = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(beasts).hasSize(1);
        assertThat(beasts.getFirst().getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void doesNotCreateTokensWhenOpponentsControlNoCreatures() {
        castEzurisPredation();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void ignoresCreaturesControlledByTheCaster() {
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player2, new HillGiant());

        castEzurisPredation();

        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(findPermanent(player1, "Air Elemental").getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(findPermanents(player1, "Phyrexian Beast")).hasSize(1);
        assertThat(findPermanent(player1, "Phyrexian Beast").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void extraTokensFromDoublingSeasonDoNotFight() {
        harness.addToBattlefield(player1, new DoublingSeason());
        addCreatureReady(player2, new AirElemental());

        castEzurisPredation();

        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(findPermanents(player1, "Phyrexian Beast")).hasSize(1);
        assertThat(findPermanent(player1, "Phyrexian Beast").getMarkedDamage()).isZero();
    }

    @Test
    void fightDamageUsesPowerBeforeVigorAddsCounters() {
        harness.addToBattlefield(player2, new Vigor());
        addCreatureReady(player2, new HillGiant());

        castEzurisPredation();

        harness.assertOnBattlefield(player2, "Vigor");
        assertThat(findPermanent(player2, "Vigor").getMarkedDamage()).isEqualTo(4);
        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getMarkedDamage()).isZero();
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(findPermanents(player1, "Phyrexian Beast")).hasSize(1);
        assertThat(findPermanent(player1, "Phyrexian Beast").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void originalTokensStillFightAfterDecliningJinnieFayReplacement() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        addCreatureReady(player2, new HillGiant());

        castEzurisPredation();
        harness.handleListChoice(player1, "Original tokens");

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(findPermanents(player1, "Phyrexian Beast")).hasSize(1);
        assertThat(findPermanent(player1, "Phyrexian Beast").getMarkedDamage()).isEqualTo(3);
        assertThat(findPermanent(player1, "Jinnie Fay, Jetmir's Second").getMarkedDamage()).isZero();
    }

    @Test
    void replacementCatTokensStillFight() {
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        addCreatureReady(player2, new HillGiant());

        castEzurisPredation();
        harness.handleListChoice(player1, "Cat");

        assertThat(findPermanents(player1, "Cat")).isEmpty();
        assertThat(findPermanents(player1, "Phyrexian Beast")).isEmpty();
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(findPermanent(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player1, "Jinnie Fay, Jetmir's Second").getMarkedDamage()).isZero();
    }

    private void castEzurisPredation() {
        harness.setHand(player1, List.of(new EzurisPredation()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
