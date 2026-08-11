package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IconOfAncestryTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type gives matching creatures +1/+1")
    void boostsCreaturesOfChosenType() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent icon = addReadyIcon();
        icon.setChosenSubtype(CardSubtype.BEAR);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activated ability offers only a creature of the chosen type")
    void activatedAbilityOffersChosenTypeCreature() {
        Permanent icon = addReadyIcon();
        icon.setChosenSubtype(CardSubtype.BEAR);
        Card bear = new GrizzlyBears();
        Card elf = new LlanowarElves();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(bear, elf, shock));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(icon), null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(bear, elf, shock);
        assertThat(choice.validCardIds()).containsExactly(bear.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(bear);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(elf, shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing the type through the engine enables the anthem")
    void choosingTypeThroughEngineEnablesAnthem() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IconOfAncestry()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    private Permanent addReadyIcon() {
        Permanent icon = harness.addToBattlefieldAndReturn(player1, new IconOfAncestry());
        icon.setSummoningSick(false);
        return icon;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
