package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EsperTerra;
import com.github.laxika.magicalvibes.cards.e.ExtravagantReplication;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerraMagicalAdept.class, EsperTerra.class, ExtravagantReplication.class,
        Forest.class, GrizzlyBears.class, HistoryOfBenalia.class, Shock.class})
class TerraMagicalAdeptTest extends BaseCardTest {

    @Test
    void entersByMillingFiveAndOffersAnEnchantmentFromThoseCards() {
        ExtravagantReplication enchantment = new ExtravagantReplication();
        harness.setLibrary(player1, List.of(
                enchantment, new Shock(), new GrizzlyBears(), new Forest(), new Shock()));
        harness.setHand(player1, List.of(new TerraMagicalAdept()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(enchantment.getId()));
    }

    @Test
    void tranceTransformsTerraIntoEsperTerraWithItsFirstLoreCounter() {
        Permanent terra = addReadyTerra();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(terra), null, null);
        harness.passBothPriorities();

        Permanent transformed = findPermanent(player1, EsperTerra.class);
        assertThat(transformed.isTransformed()).isTrue();
        assertThat(transformed.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void chaptersCopyAnEnchantmentWithHasteAndSacrificeItAtTheNextEndStep() {
        Permanent terra = addEsperTerraWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ExtravagantReplication());

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));

        assertThat(terra.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void copiedSagaCanChooseUpToThreeAdditionalLoreCounters() {
        addEsperTerraWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HistoryOfBenalia());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        for (int i = 0; i < 4 && !gd.interaction.isAwaitingInput(); i++) {
            harness.passBothPriorities();
        }

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCounterCount(CounterType.LORE)).isEqualTo(1);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isEqualTo(new ChoiceContext.NumberChoice(token.getId()));
        assertThat(choice.options()).containsExactly("0", "1", "2", "3");

        harness.handleListChoice(player1, "0");
        assertThat(token.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void fourthChapterAddsTwoOfEachColorAndReturnsToTheFrontFace() {
        Permanent terra = addEsperTerraWithLore(3);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        Permanent returned = findPermanent(player1, TerraMagicalAdept.class);
        assertThat(returned.isTransformed()).isFalse();
    }

    private Permanent addReadyTerra() {
        Permanent terra = harness.addToBattlefieldAndReturn(player1, new TerraMagicalAdept());
        terra.setSummoningSick(false);
        return terra;
    }

    private Permanent addEsperTerraWithLore(int loreCounters) {
        TerraMagicalAdept front = new TerraMagicalAdept();
        Permanent terra = new Permanent(front);
        terra.setCard(front.getBackFaceCard());
        terra.setTransformed(true);
        terra.setSummoningSick(false);
        terra.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player1.getId()).add(terra);
        return terra;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Player player, Class<?> cardClass) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> cardClass.isInstance(permanent.getCard()))
                .findFirst()
                .orElseThrow();
    }
}
