package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChatterfangSquirrelGeneral;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.j.JinnieFayJetmirsSecond;
import com.github.laxika.magicalvibes.cards.v.Vigor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EzurisPredation.class, HillGiant.class, AirElemental.class, DoublingSeason.class, Vigor.class, JinnieFayJetmirsSecond.class,
        ChatterfangSquirrelGeneral.class})
class EzurisPredationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Beast for each opposing creature and each fights a different one")
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
    @DisplayName("Creates nothing when opponents control no creatures")
    void doesNotCreateTokensWhenOpponentsControlNoCreatures() {
        castEzurisPredation();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Ignores the caster's own creatures")
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
    @DisplayName("The extra tokens Doubling Season makes fight nothing (ruling)")
    void extraTokensFromDoublingSeasonDoNotFight() {
        harness.addToBattlefield(player1, new DoublingSeason());
        addCreatureReady(player2, new AirElemental());

        castEzurisPredation();

        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(findPermanents(player1, "Phyrexian Beast")).hasSize(1);
        assertThat(findPermanent(player1, "Phyrexian Beast").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Every fight reads power before any damage, so Vigor's counters come too late")
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
    @DisplayName("The original tokens still fight after Jinnie Fay's replacement is declined")
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
    @DisplayName("Jinnie Fay's replacement tokens fight in their place")
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

    @Test
    @DisplayName("Its caster chooses which of those tokens fights which creature (ruling)")
    void casterChoosesWhichTokenFightsWhichCreature() {
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());
        Permanent giant = addCreatureReady(player2, new HillGiant());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        castEzurisPredation();

        PendingInteraction.PermanentChoice pairing = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(pairing).isNotNull();
        assertThat(pairing.validIds()).hasSize(4);
        Permanent squirrel = findPermanents(player1, "Squirrel").getFirst();
        harness.handlePermanentChosen(player1, squirrel.getId());
        Permanent beast = findPermanents(player1, "Phyrexian Beast").getFirst();
        harness.handlePermanentChosen(player1, beast.getId());

        assertThat(giant.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(findPermanents(player1, "Phyrexian Beast")).hasSize(1);
        assertThat(findPermanents(player1, "Squirrel")).hasSize(1);
    }

    private void castEzurisPredation() {
        harness.setHand(player1, List.of(new EzurisPredation()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
