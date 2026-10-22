package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WizardsSpellbook.class, Divination.class, GrizzlyBears.class})
class WizardsSpellbookTest extends BaseCardTest {

    private RollD20EffectHandler rollD20EffectHandler;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureD20RollService() {
        rollD20EffectHandler = GameTestEngineContext.get().getBean(RollD20EffectHandler.class);
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(
                rollD20EffectHandler, "d20RollService");
    }

    @AfterEach
    void restoreD20RollService() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", originalD20RollService);
    }

    @Test
    void copiesTargetedCardForNormalCostOnLowRoll() {
        setRoll(1);
        WizardsSpellbook spellbook = new WizardsSpellbook();
        Divination divination = new Divination();
        harness.addToBattlefield(player1, spellbook);
        harness.setGraveyard(player1, List.of(divination));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, divination.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(2);
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Wizard's Spellbook").getId()))
                .containsExactly(divination);
    }

    @Test
    void copiesTargetedCardForOneGenericOnMiddleRoll() {
        setRoll(10);
        WizardsSpellbook spellbook = new WizardsSpellbook();
        Divination divination = new Divination();
        harness.addToBattlefield(player1, spellbook);
        harness.setGraveyard(player1, List.of(divination));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, divination.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(2);
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Wizard's Spellbook").getId()))
                .containsExactly(divination);
    }

    @Test
    @CardUsed({WearTear.class, FountainOfYouth.class, AngelicChorus.class})
    @DisplayName("A split card's copy is cast as the chosen half for {1} rather than that half's mana cost")
    void splitCardCopyIsCastAsTheChosenHalfForOneGeneric() {
        setRoll(10);
        WearTear wearTear = new WearTear();
        harness.addToBattlefield(player1, new WizardsSpellbook());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID chorusId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.setGraveyard(player1, List.of(wearTear));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, wearTear.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Wear — Destroy target artifact", "Tear — Destroy target enchantment");
        harness.handleListChoice(player1, "Tear — Destroy target enchantment");
        assertThat(gd.stack).isEmpty();
        harness.handlePermanentChosen(player1, chorusId);

        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Tear");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Wizard's Spellbook").getId()))
                .containsExactly(wearTear);
    }

    @Test
    void twentyCopiesAllCardsExiledWithSpellbook() {
        setRolls(1, 20);
        WizardsSpellbook spellbook = new WizardsSpellbook();
        Divination first = new Divination();
        Divination second = new Divination();
        harness.addToBattlefield(player1, spellbook);
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, first.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        findPermanent(player1, "Wizard's Spellbook").untap();
        harness.activateAbility(player1, 0, null, second.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(4);
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Wizard's Spellbook").getId()))
                .containsExactly(first, second);
    }

    @Test
    void copiesCardFromOpponentsGraveyardUnderActivatorsControl() {
        setRoll(19);
        Divination divination = new Divination();
        harness.addToBattlefield(player1, new WizardsSpellbook());
        harness.setGraveyard(player2, List.of(divination));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, divination.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(divination);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(harness.getPermanentId(player1, "Wizard's Spellbook")))
                .containsExactly(divination);
    }

    @Test
    void decliningCopyLeavesOnlyOriginalInExile() {
        setRoll(9);
        Divination divination = new Divination();
        harness.addToBattlefield(player1, new WizardsSpellbook());
        harness.setGraveyard(player1, List.of(divination));

        harness.activateAbility(player1, 0, null, divination.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(divination);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void illegalGraveyardTargetPreventsCopies() {
        setRoll(20);
        Divination divination = new Divination();
        harness.addToBattlefield(player1, new WizardsSpellbook());
        harness.setGraveyard(player1, List.of(divination));

        harness.activateAbility(player1, 0, null, divination.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void setRoll(int result) {
        setRolls(result);
    }

    private void setRolls(int... results) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService",
                new SequentialD20RollService(results));
    }

    private static final class SequentialD20RollService extends D20RollService {

        private final int[] results;
        private int index;

        private SequentialD20RollService(int[] results) {
            this.results = results;
        }

        @Override
        public int roll() {
            return results[Math.min(index++, results.length - 1)];
        }
    }
}
