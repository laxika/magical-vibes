package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShiftingCeratopsTest extends BaseCardTest {

    @Test
    @DisplayName("Shifting Ceratops cannot be countered by Cancel")
    void cannotBeCountered() {
        ShiftingCeratops ceratops = new ShiftingCeratops();
        harness.setHand(player1, List.of(ceratops));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ceratops.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shifting Ceratops");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Blue spells cannot target Shifting Ceratops")
    void cannotBeTargetedByBlueSpell() {
        Permanent ceratops = addCeratops(player2);
        harness.setHand(player1, List.of(createBlueInstant()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ceratops.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shifting Ceratops can choose each granted keyword")
    void choosesReachTrampleOrHaste() {
        Permanent ceratops = addCeratops(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        for (String keyword : List.of("REACH", "TRAMPLE", "HASTE")) {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
            harness.handleListChoice(player1, keyword);
            assertThat(gqs.hasKeyword(gd, ceratops, Keyword.valueOf(keyword))).isTrue();
        }
    }

    @Test
    @DisplayName("Shifting Ceratops loses its chosen keyword at end of turn")
    void chosenKeywordResetsAtEndOfTurn() {
        Permanent ceratops = addCeratops(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "TRAMPLE");

        assertThat(gqs.hasKeyword(gd, ceratops, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ceratops, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addCeratops(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new ShiftingCeratops());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private static Card createBlueInstant() {
        Card card = new Card();
        card.setName("Blue Bolt");
        card.setType(CardType.INSTANT);
        card.setManaCost("{U}");
        card.setColor(CardColor.BLUE);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }
}
