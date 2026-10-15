package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoSecretIsHiddenFromMe.class, Forest.class, GrizzlyBears.class})
class NoSecretIsHiddenFromMeTest extends BaseCardTest {

    @Test
    void exilesUntilNonlandAndOffersTheFirstCardForFree() {
        Card land = new Forest();
        Card spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, spell));

        resolveScheme();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == spell
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(land);
    }

    @Test
    void repeatsOnceWhenControllerHasSixLands() {
        addLands(6);
        Card firstLand = new Forest();
        Card firstSpell = new GrizzlyBears();
        Card secondLand = new Forest();
        Card secondSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstLand, firstSpell, secondLand, secondSpell));

        resolveScheme();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL)
                .extracting(StackEntry::getCard)
                .containsExactlyInAnyOrder(firstSpell, secondSpell);
        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .containsExactly(firstLand, secondLand);
    }

    @Test
    void doesNotRepeatWithFewerThanSixLands() {
        addLands(5);
        Card firstLand = new Forest();
        Card firstSpell = new GrizzlyBears();
        Card secondLand = new Forest();
        Card secondSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstLand, firstSpell, secondLand, secondSpell));

        resolveScheme();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL)
                .extracting(StackEntry::getCard)
                .containsExactly(firstSpell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondLand, secondSpell);
    }

    private void resolveScheme() {
        NoSecretIsHiddenFromMe scheme = new NoSecretIsHiddenFromMe();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }

    private void addLands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }
}
