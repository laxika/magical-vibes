package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.o.OrazcaFrillback;
import com.github.laxika.magicalvibes.cards.t.TombOfTheDuskRose;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProfaneProcession.class, TombOfTheDuskRose.class, OrazcaFrillback.class})
class ProfaneProcessionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature and tracks it without transforming below three cards")
    void exilesAndTracksCreatureBelowThreshold() {
        Permanent procession = addReadyProcession(player1);
        Permanent creature = addCreatureReady(player2, new OrazcaFrillback());
        addProcessionMana(1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(procession.getId()))
                .extracting(Card::getName)
                .containsExactly("Orazca Frillback");
        assertThat(procession.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transforms after the third non-token creature is exiled")
    void transformsAfterThirdExiledCreature() {
        Permanent procession = addReadyProcession(player1);
        Permanent first = addCreatureReady(player2, new OrazcaFrillback());
        Permanent second = addCreatureReady(player2, new OrazcaFrillback());
        Permanent third = addCreatureReady(player2, new OrazcaFrillback());
        addProcessionMana(3);

        exileTarget(first);
        exileTarget(second);
        exileTarget(third);

        assertThat(procession.isTransformed()).isTrue();
        assertThat(procession.getCard().getName()).isEqualTo("Tomb of the Dusk Rose");
        assertThat(gd.getCardsExiledByPermanent(procession.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A token creature does not count toward the transform threshold")
    void tokenDoesNotCountTowardThreshold() {
        Permanent procession = addReadyProcession(player1);
        Permanent token = addCreatureReady(player2, tokenCreature());
        Permanent first = addCreatureReady(player2, new OrazcaFrillback());
        Permanent second = addCreatureReady(player2, new OrazcaFrillback());
        addProcessionMana(3);

        exileTarget(token);
        exileTarget(first);
        exileTarget(second);

        assertThat(procession.isTransformed()).isFalse();
        assertThat(gd.getCardsExiledByPermanent(procession.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Tomb of the Dusk Rose returns a tracked creature under your control")
    void returnsTrackedCreature() {
        ProfaneProcession card = new ProfaneProcession();
        Permanent tomb = new Permanent(card);
        tomb.setSummoningSick(false);
        tomb.setCard(card.getBackFaceCard());
        tomb.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(tomb);

        Card creature = new OrazcaFrillback();
        gd.addToExile(player2.getId(), creature, tomb.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Orazca Frillback").getCard().getId())
                .isEqualTo(creature.getId());
        assertThat(tomb.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pending activations exile additional creatures without transforming the Tomb back")
    void pendingActivationsDoNotTransformBack() {
        Permanent procession = addReadyProcession(player1);
        Permanent first = addCreatureReady(player2, new OrazcaFrillback());
        Permanent second = addCreatureReady(player2, new OrazcaFrillback());
        Permanent third = addCreatureReady(player2, new OrazcaFrillback());
        Permanent fourth = addCreatureReady(player2, new OrazcaFrillback());
        addProcessionMana(4);

        exileTarget(first);
        exileTarget(second);
        harness.activateAbility(player1, 0, 0, null, third.getId());
        harness.activateAbility(player1, 0, 0, null, fourth.getId());
        resolveAllTriggers();

        assertThat(procession.isTransformed()).isTrue();
        assertThat(procession.getCard().getName()).isEqualTo("Tomb of the Dusk Rose");
        assertThat(gd.getCardsExiledByPermanent(procession.getId())).hasSize(4);
    }

    @Test
    @DisplayName("The transformed Tomb adds a chosen color of mana immediately")
    void tombAddsChosenMana() {
        Permanent procession = transformProcession();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(procession.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Tomb chooses one linked creature and leaves unrelated exiled cards alone")
    void choosesOneLinkedCreature() {
        Permanent tomb = transformProcession();
        Card chosen = gd.getCardsExiledByPermanent(tomb.getId()).get(1);
        Card unrelated = new OrazcaFrillback();
        gd.addToExile(player2.getId(), unrelated);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(findPermanent(player1, "Orazca Frillback").getCard().getId()).isEqualTo(chosen.getId());
        assertThat(gd.getCardsExiledByPermanent(tomb.getId())).hasSize(2);
        assertThat(gd.findExiledCard(unrelated.getId())).isNotNull();
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
        assertThat(tomb.isTapped()).isTrue();
    }

    private Permanent transformProcession() {
        Permanent procession = addReadyProcession(player1);
        addProcessionMana(3);
        for (int i = 0; i < 3; i++) {
            exileTarget(addCreatureReady(player2, new OrazcaFrillback()));
        }
        return procession;
    }

    private Permanent addReadyProcession(Player player) {
        return addCreatureReady(player, new ProfaneProcession());
    }

    private void addProcessionMana(int activations) {
        harness.addMana(player1, ManaColor.COLORLESS, 3 * activations);
        harness.addMana(player1, ManaColor.WHITE, activations);
        harness.addMana(player1, ManaColor.BLACK, activations);
    }

    private void exileTarget(Permanent target) {
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private Card tokenCreature() {
        Card token = new Card();
        token.setName("Token Creature");
        token.setType(CardType.CREATURE);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
