package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoiceOfTheWoods.class, ElvishWarrior.class})
class VoiceOfTheWoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping five Elves creates a 7/7 green Elemental with trample")
    void createsElementalByTappingFiveElves() {
        Permanent source = addCreatureReady(player1, new VoiceOfTheWoods());
        Permanent firstElf = addCreatureReady(player1, new ElvishWarrior());
        Permanent secondElf = addCreatureReady(player1, new ElvishWarrior());
        Permanent thirdElf = addCreatureReady(player1, new ElvishWarrior());
        Permanent fourthElf = addCreatureReady(player1, new ElvishWarrior());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(firstElf.isTapped()).isTrue();
        assertThat(secondElf.isTapped()).isTrue();
        assertThat(thirdElf.isTapped()).isTrue();
        assertThat(fourthElf.isTapped()).isTrue();

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token.getEffectivePower()).isEqualTo(7);
        assertThat(token.getEffectiveToughness()).isEqualTo(7);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Cannot activate without five untapped Elves")
    void requiresFiveUntappedElves() {
        Permanent source = addCreatureReady(player1, new VoiceOfTheWoods());
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new ElvishWarrior());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
