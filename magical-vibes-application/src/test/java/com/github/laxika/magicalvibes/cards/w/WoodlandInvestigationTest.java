package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodlandInvestigation.class, Forest.class, Plains.class})
class WoodlandInvestigationTest extends BaseCardTest {

    @Test
    void searchesForABasicLandAndMakesTheChosenLandAClue() {
        harness.setHand(player1, List.of(new WoodlandInvestigation()));
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2);
        harness.handleCardChosen(player1, 0);

        harness.playLand(player1, 0);
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isArtifact(gd, forest)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.CLUE)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
  harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest.getOriginalCard());
    }
}
