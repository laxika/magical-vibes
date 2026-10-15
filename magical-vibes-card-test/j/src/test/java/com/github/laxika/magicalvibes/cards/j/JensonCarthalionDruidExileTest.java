package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CoilingOracle;
import com.github.laxika.magicalvibes.cards.f.FusionElemental;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JensonCarthalionDruidExile.class, FusionElemental.class, CoilingOracle.class, MycosynthLattice.class})
class JensonCarthalionDruidExileTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an all-color spell scries 1 and creates a 4/4 Angel")
    void allColorSpellScriesAndCreatesAngel() {
        harness.addToBattlefield(player1, new JensonCarthalionDruidExile());
        List<Card> library = List.of(new JensonCarthalionDruidExile());
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new FusionElemental(), "{W}{U}{B}{R}{G}");
        resolveTriggerAndScry();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        Permanent angel = findPermanent(player1, "Angel");
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The mana ability adds one mana of each color")
    void manaAbilityAddsAllFiveColors() {
        Permanent jenson = harness.addToBattlefieldAndReturn(player1, new JensonCarthalionDruidExile());
        jenson.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(jenson.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A two-color spell scries without creating an Angel")
    void twoColorSpellOnlyScries() {
        harness.addToBattlefield(player1, new JensonCarthalionDruidExile());
        Card top = new FusionElemental();
        harness.setLibrary(player1, List.of(top));
        harness.castFromHand(player1, new CoilingOracle(), "{G}{U}");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Angel"));
        resolveAllTriggers();
    }

    @Test
    @DisplayName("An opponent's all-color spell does not trigger Jenson")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new JensonCarthalionDruidExile());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new FusionElemental(), "{W}{U}{B}{R}{G}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A colorless spell does not trigger Jenson")
    void colorlessSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new JensonCarthalionDruidExile());
        harness.castFromHand(player1, new MycosynthLattice(), "{6}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Mycosynth Lattice makes an otherwise multicolored spell colorless")
    void latticePreventsMulticoloredCastTrigger() {
        harness.addToBattlefield(player1, new JensonCarthalionDruidExile());
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setLibrary(player1, List.of(new CoilingOracle()));
        harness.castFromHand(player1, new FusionElemental(), "{W}{U}{B}{R}{G}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Angel"));
    }

    private void resolveTriggerAndScry() {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
    }
}
