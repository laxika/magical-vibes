package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KarametraGodOfHarvestsTest extends BaseCardTest {

    @Test
    @DisplayName("Karametra is not a creature below seven combined green and white devotion")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent karametra = addKarametra();
        addGreenAndWhitePermanents(4);

        assertThat(gqs.isCreature(gd, karametra)).isFalse();
        assertThat(gqs.isEnchantment(gd, karametra)).isTrue();
    }

    @Test
    @DisplayName("Karametra becomes a creature at seven combined green and white devotion")
    void becomesCreatureAtDevotionThreshold() {
        Permanent karametra = addKarametra();
        addGreenAndWhitePermanents(5);

        assertThat(gqs.isCreature(gd, karametra)).isTrue();
    }

    @Test
    @DisplayName("Casting a creature may search for a Forest or Plains and put it onto the battlefield tapped")
    void creatureSpellMaySearchForForestOrPlains() {
        addKarametra();
        Forest forest = new Forest();
        Plains plains = new Plains();
        setLibrary(forest, plains, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, plains);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent fetched = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest || permanent.getCard() == plains)
                .findFirst()
                .orElseThrow();
        assertThat(fetched.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the creature spell trigger does not search")
    void decliningSearchDoesNothing() {
        addKarametra();
        setLibrary(new Forest(), new Plains());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest || permanent.getCard() instanceof Plains);
    }

    private Permanent addKarametra() {
        return harness.addToBattlefieldAndReturn(player1, new KarametraGodOfHarvests());
    }

    private void addGreenAndWhitePermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, i % 2 == 0 ? new GrizzlyBears() : new GlorySeeker());
        }
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
