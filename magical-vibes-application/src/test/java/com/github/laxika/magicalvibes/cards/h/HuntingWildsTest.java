package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntingWilds.class, Forest.class, Island.class, GrizzlyBears.class})
class HuntingWildsTest extends BaseCardTest {

    @Test
    void searchesForUpToTwoForests() {
        harness.setHand(player1, List.of(new HuntingWilds()));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Forest)
                .singleElement()
                .matches(permanent -> permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Island);
    }

    @Test
    void kickedSpellMakesFetchedForestsPermanentHastyThreeThreeGreenCreaturesAndUntapsThem() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new HuntingWilds()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Island(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        List<Permanent> forests = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Forest)
                .toList();
        assertThat(forests).hasSize(3);
        assertThat(forests).allMatch(permanent -> gqs.isLand(gd, permanent));
        assertThat(forests).filteredOn(permanent -> gqs.isCreature(gd, permanent)).hasSize(2);
        assertThat(forests).filteredOn(permanent -> !gqs.isCreature(gd, permanent)).hasSize(1);
        assertThat(forests).filteredOn(permanent -> gqs.isCreature(gd, permanent))
                .allMatch(permanent -> !permanent.isTapped())
                .allMatch(permanent -> gqs.hasColor(gd, permanent, CardColor.GREEN))
                .allMatch(permanent -> gqs.getEffectivePower(gd, permanent) == 3)
                .allMatch(permanent -> gqs.getEffectiveToughness(gd, permanent) == 3)
                .allMatch(permanent -> gqs.hasKeyword(gd, permanent, Keyword.HASTE));
    }
}
