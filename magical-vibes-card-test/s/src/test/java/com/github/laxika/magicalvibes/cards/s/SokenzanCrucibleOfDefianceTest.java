package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AkkiRonin;
import com.github.laxika.magicalvibes.cards.h.HeikoYamazakiTheGeneral;
import com.github.laxika.magicalvibes.cards.i.IsshinTwoHeavensAsOne;
import com.github.laxika.magicalvibes.cards.n.NorikaYamazakiThePoet;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SokenzanCrucibleOfDefiance.class, HeikoYamazakiTheGeneral.class,
        IsshinTwoHeavensAsOne.class, NorikaYamazakiThePoet.class, AkkiRonin.class})
class SokenzanCrucibleOfDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("Adds red mana")
    void addsRedMana() {
        harness.addToBattlefield(player1, new SokenzanCrucibleOfDefiance());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Channel creates two hasty Spirit tokens with legendary creature cost reductions")
    void channelCreatesHastySpiritTokensWithLegendaryCostReduction() {
        harness.addToBattlefield(player1, new IsshinTwoHeavensAsOne());
        harness.addToBattlefield(player1, new HeikoYamazakiTheGeneral());

        harness.setHand(player1, List.of(new SokenzanCrucibleOfDefiance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allMatch(permanent -> permanent.hasKeyword(Keyword.HASTE));
        harness.assertInGraveyard(player1, "Sokenzan, Crucible of Defiance");
    }

    @Test
    @DisplayName("Channel-granted haste expires at the end of the turn")
    void channelGrantedHasteExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new IsshinTwoHeavensAsOne());
        harness.addToBattlefield(player1, new HeikoYamazakiTheGeneral());

        harness.setHand(player1, List.of(new SokenzanCrucibleOfDefiance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).allMatch(permanent -> permanent.hasKeyword(Keyword.HASTE));

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(findPermanents(player1, "Spirit")).allMatch(permanent -> !permanent.hasKeyword(Keyword.HASTE));
    }

    @Test
    @DisplayName("Channel pays its full cost and discards before its tokens resolve")
    void channelPaysFullCostAndUsesStack() {
        harness.setHand(player1, List.of(new SokenzanCrucibleOfDefiance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Sokenzan, Crucible of Defiance");
        harness.assertInGraveyard(player1, "Sokenzan, Crucible of Defiance");
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
        });
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Channel cannot be activated without enough generic mana")
    void channelRejectsInsufficientManaWithoutDiscarding() {
        harness.setHand(player1, List.of(new SokenzanCrucibleOfDefiance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Sokenzan, Crucible of Defiance");
        harness.assertNotInGraveyard(player1, "Sokenzan, Crucible of Defiance");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only legendary creatures controlled by the activating player reduce Channel's cost")
    void channelIgnoresNonlegendaryCreaturesLegendaryLandsAndOpponentsCreatures() {
        harness.addToBattlefield(player1, new AkkiRonin());
        harness.addToBattlefield(player1, new SokenzanCrucibleOfDefiance());
        harness.addToBattlefield(player2, new IsshinTwoHeavensAsOne());
        harness.setHand(player1, List.of(new SokenzanCrucibleOfDefiance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Sokenzan, Crucible of Defiance");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Three legendary creatures reduce Channel's generic cost to zero")
    void channelCanCostJustOneRedMana() {
        harness.addToBattlefield(player1, new IsshinTwoHeavensAsOne());
        harness.addToBattlefield(player1, new HeikoYamazakiTheGeneral());
        harness.addToBattlefield(player1, new NorikaYamazakiThePoet());
        harness.setHand(player1, List.of(new SokenzanCrucibleOfDefiance()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Legendary creatures do not reduce Channel's red mana requirement")
    void channelStillRequiresRedMana() {
        harness.addToBattlefield(player1, new IsshinTwoHeavensAsOne());
        harness.addToBattlefield(player1, new HeikoYamazakiTheGeneral());
        harness.addToBattlefield(player1, new NorikaYamazakiThePoet());
        harness.setHand(player1, List.of(new SokenzanCrucibleOfDefiance()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Sokenzan, Crucible of Defiance");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Channel is available during an opponent's end step with one legendary creature")
    void channelCanBeActivatedOnOpponentsTurn() {
        harness.addToBattlefield(player1, new HeikoYamazakiTheGeneral());
        harness.setHand(player1, List.of(new SokenzanCrucibleOfDefiance()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allMatch(token -> token.hasKeyword(Keyword.HASTE));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(findPermanents(player1, "Spirit")).hasSize(2)
                .allMatch(token -> !token.hasKeyword(Keyword.HASTE));
    }
}
