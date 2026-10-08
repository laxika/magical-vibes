package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.cards.t.ThreeTreeMascot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeenEyedCurator.class, Forest.class, GrizzlyBears.class, Shock.class, TormodsCrypt.class,
        ThreeTreeMascot.class})
class KeenEyedCuratorTest extends BaseCardTest {

    @Test
    @DisplayName("It is a 3/3 without four card types exiled with it")
    void noBonusBeforeFourExiledCardTypes() {
        Permanent curator = addCurator();

        assertThat(gqs.getEffectivePower(gd, curator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, curator)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, curator, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Exiling four card types gives it +4/+4 and trample")
    void gainsBonusAfterExilingFourCardTypes() {
        Permanent curator = addCurator();
        List<com.github.laxika.magicalvibes.model.Card> cards = List.of(
                new Forest(), new Shock(), new GrizzlyBears(), new TormodsCrypt());
        harness.setGraveyard(player1, cards);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (var card : cards) {
            harness.activateAbility(player1, 0, null, card.getId(), Zone.GRAVEYARD);
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, curator)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, curator)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, curator, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.exiledCards).allMatch(entry -> curator.getId().equals(entry.sourcePermanentId()));
    }

    @Test
    @DisplayName("Its ability cannot target a permanent instead of a graveyard card")
    void cannotTargetPermanent() {
        addCurator();
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Four creature cards from the opponent's graveyard still count as only one card type")
    void duplicateTypesDoNotReachThreshold() {
        Permanent curator = addCurator();
        List<com.github.laxika.magicalvibes.model.Card> cards = List.of(
                new KeenEyedCurator(), new KeenEyedCurator(), new KeenEyedCurator(), new KeenEyedCurator());
        harness.setGraveyard(player2, cards);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (var card : cards) {
            harness.activateAbility(player1, 0, null, card.getId(), Zone.GRAVEYARD);
            harness.passBothPriorities();
        }

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(4);
        assertThat(gqs.getEffectivePower(gd, curator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, curator)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, curator, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An artifact creature supplies two card types toward the threshold")
    void multipleTypesOnOneCardCountSeparately() {
        Permanent curator = addCurator();
        List<com.github.laxika.magicalvibes.model.Card> cards = List.of(
                new ThreeTreeMascot(), new Forest(), new Shock());
        harness.setGraveyard(player2, cards);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int i = 0; i < cards.size(); i++) {
            harness.activateAbility(player1, 0, null, cards.get(i).getId(), Zone.GRAVEYARD);
            harness.passBothPriorities();

            assertThat(gqs.getEffectivePower(gd, curator)).isEqualTo(i == 2 ? 7 : 3);
            assertThat(gqs.getEffectiveToughness(gd, curator)).isEqualTo(i == 2 ? 7 : 3);
            assertThat(gqs.hasKeyword(gd, curator, Keyword.TRAMPLE)).isEqualTo(i == 2);
        }

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(3);
    }

    @Test
    @DisplayName("Each Curator counts only cards exiled with itself")
    void separateCuratorsDoNotShareExiledCardTypes() {
        Permanent firstCurator = addCurator();
        Permanent secondCurator = addCurator();
        List<com.github.laxika.magicalvibes.model.Card> cards = List.of(
                new Forest(), new Shock(), new GrizzlyBears(), new TormodsCrypt());
        harness.setGraveyard(player1, cards);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (var card : cards) {
            harness.activateAbility(player1, 0, null, card.getId(), Zone.GRAVEYARD);
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, firstCurator)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, firstCurator)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, firstCurator, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, secondCurator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondCurator)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, secondCurator, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cards exiled by other effects do not count toward the threshold")
    void unrelatedExiledCardsDoNotCount() {
        Permanent curator = addCurator();
        harness.setExile(player1, List.of(new Forest(), new Shock(), new GrizzlyBears(), new TormodsCrypt()));

        assertThat(gqs.getEffectivePower(gd, curator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, curator)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, curator, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ability requires one mana but can be used while tapped and summoning sick")
    void activationRequiresManaWithoutTapOrSummoningSicknessRestriction() {
        Permanent curator = harness.addToBattlefieldAndReturn(player1, new KeenEyedCurator());
        curator.tap();
        KeenEyedCurator card = new KeenEyedCurator();
        harness.setGraveyard(player1, List.of(card));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, card.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Keen-Eyed Curator");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, card.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Keen-Eyed Curator");
        assertThat(gd.exiledCards).singleElement().satisfies(entry -> {
            assertThat(entry.card()).isSameAs(card);
            assertThat(entry.sourcePermanentId()).isEqualTo(curator.getId());
        });
        assertThat(curator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An activation does nothing if a later activation already exiled its target")
    void targetLeavingGraveyardBeforeResolutionIsNotExiledTwice() {
        addCurator();
        KeenEyedCurator card = new KeenEyedCurator();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, card.getId(), Zone.GRAVEYARD);
        harness.activateAbility(player1, 0, null, card.getId(), Zone.GRAVEYARD);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).singleElement().satisfies(entry ->
                assertThat(entry.card()).isSameAs(card));
        harness.assertNotInGraveyard(player1, "Keen-Eyed Curator");
    }

    private Permanent addCurator() {
        Permanent curator = harness.addToBattlefieldAndReturn(player1, new KeenEyedCurator());
        curator.setSummoningSick(false);
        return curator;
    }
}
