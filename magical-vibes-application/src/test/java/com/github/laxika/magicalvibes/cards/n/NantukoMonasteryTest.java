package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AvenFogbringer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NantukoMonastery.class, AvenFogbringer.class})
class NantukoMonasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Nantuko Monastery produces one colorless mana")
    void tapForColorless() {
        addCreatureReady(player1, new NantukoMonastery());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The animation ability cannot be activated without threshold")
    void animationRequiresThreshold() {
        addCreatureReady(player1, new NantukoMonastery());
        harness.setGraveyard(player1, graveyardCards(6));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Threshold animates Nantuko Monastery as a 4/4 green-white Insect Monk with first strike")
    void thresholdAnimatesMonastery() {
        Permanent monastery = addCreatureReady(player1, new NantukoMonastery());
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(monastery.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, monastery)).isTrue();
        assertThat(gqs.isLand(gd, monastery)).isTrue();
        assertThat(gqs.getEffectivePower(gd, monastery)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, monastery)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, monastery))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(gqs.hasEffectiveSubtype(gd, monastery, CardSubtype.INSECT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, monastery, CardSubtype.MONK)).isTrue();
        assertThat(gqs.hasKeyword(gd, monastery, Keyword.FIRST_STRIKE)).isTrue();

        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The animation ends at the end of the turn")
    void animationEndsAtEndOfTurn() {
        Permanent monastery = addCreatureReady(player1, new NantukoMonastery());
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, monastery)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monastery)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, monastery)).isEmpty();
        assertThat(gqs.hasEffectiveSubtype(gd, monastery, CardSubtype.INSECT)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, monastery, CardSubtype.MONK)).isFalse();
        assertThat(gqs.hasKeyword(gd, monastery, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Threshold does not count cards in an opponent's graveyard")
    void thresholdRequiresControllerGraveyard() {
        addCreatureReady(player1, new NantukoMonastery());
        harness.setGraveyard(player2, graveyardCards(7));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards in your graveyard");
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new AvenFogbringer());
        }
        return cards;
    }
}
