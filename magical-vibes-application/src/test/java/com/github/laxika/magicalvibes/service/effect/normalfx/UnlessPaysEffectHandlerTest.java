package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnlessPaysEffectHandlerTest {

    @Mock private SubjectResolutionService subjectResolutionService;
    @Mock private AmountEvaluationService amountEvaluationService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameLogService gameLogService;
    @Mock private LifeSupport lifeSupport;

    private UnlessPaysEffectHandler handler;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private StackEntry spell;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerManaPools.put(player1Id, new ManaPool());
        Card elves = new Card();
        elves.setName("Llanowar Elves");
        elves.setType(CardType.CREATURE);
        spell = new StackEntry(StackEntryType.CREATURE_SPELL, elves, player1Id, elves.getName(), List.of());
        gd.stack.add(spell);
        lenient().when(subjectResolutionService.spellOnStack(any(), any(), any())).thenReturn(spell);
        handler = new UnlessPaysEffectHandler(subjectResolutionService, amountEvaluationService, gameQueryService,
                gameLogService, lifeSupport);
    }

    private StackEntry counterEntry(UnlessPaysEffect effect) {
        Card hatchling = new Card();
        hatchling.setName("Spiketail Hatchling");
        return TestStackDoor.triggered(hatchling, player2Id, hatchling.getName(), List.of(effect), 0,
                spell.getCard().getId(), null);
    }

    private void payerIsTheSpellsController(StackEntry entry, UnlessPaysEffect effect) {
        when(subjectResolutionService.player(gd, entry, effect, effect.payer())).thenReturn(player1Id);
    }

    @Test
    @DisplayName("Asks the spell's controller, naming the cost and the spell, when they can pay")
    void asksThePayerWhoCanPay() {
        gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 1);
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPays(1);
        StackEntry entry = counterEntry(effect);
        payerIsTheSpellsController(entry, effect);

        assertThat(handler.cannotBePaid(gd, entry, effect)).isFalse();
        handler.resolve(gd, entry, effect);

        assertThat(gd.resolvingMayEffectFromStack).isTrue();
        assertThat(gd.pendingMayAbilities).hasSize(1);
        PendingMayAbility ability = gd.pendingMayAbilities.getFirst();
        assertThat(ability.controllerId()).isEqualTo(player1Id);
        assertThat(ability.targetCardId()).isEqualTo(spell.getCard().getId());
        assertThat(ability.manaCost()).isEqualTo("{1}");
        assertThat(ability.description()).isEqualTo("Pay {1} to prevent Llanowar Elves from being countered?");
        assertThat(ability.effects()).containsExactly(effect);
        assertThat(ability.sourceControllerId()).isEqualTo(player2Id);
    }

    @Test
    @DisplayName("A payer without the mana can't pay, so isn't asked")
    void payerWithoutTheManaCantPay() {
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPays(1);
        StackEntry entry = counterEntry(effect);
        payerIsTheSpellsController(entry, effect);

        assertThat(handler.cannotBePaid(gd, entry, effect)).isTrue();
    }

    @Test
    @DisplayName("Nothing is asked once the spell to counter left the stack (countered \"instead\" by an earlier instruction)")
    void nothingAskedOnceTheSpellLeft() {
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPays(1);
        StackEntry entry = counterEntry(effect);
        when(subjectResolutionService.spellOnStack(gd, entry, Subjects.targetSpell()))
                .thenReturn(null);

        assertThat(handler.cannotBePaid(gd, entry, effect)).isFalse();
        handler.resolve(gd, entry, effect);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.resolvingMayEffectFromStack).isFalse();
    }

    @Test
    @DisplayName("A life ransom asks for the life, with no mana on the prompt")
    void lifeRansomAsksForLife() {
        gd.playerLifeTotals.put(player1Id, 20);
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPaysLife(3);
        StackEntry entry = counterEntry(effect);
        payerIsTheSpellsController(entry, effect);
        when(amountEvaluationService.evaluate(eq(gd), any(), any())).thenReturn(3);
        when(gameQueryService.canPlayerLoseLife(gd, player1Id)).thenReturn(true);

        assertThat(handler.cannotBePaid(gd, entry, effect)).isFalse();
        handler.resolve(gd, entry, effect);

        PendingMayAbility ability = gd.pendingMayAbilities.getFirst();
        assertThat(ability.manaCost()).isNull();
        assertThat(ability.additionalLifeCost()).isEqualTo(3);
        assertThat(ability.description()).isEqualTo("Pay 3 life to prevent Llanowar Elves from being countered?");
    }

    @Test
    @DisplayName("A player who can't lose life can't pay a life ransom (CR 119.8)")
    void playerWhoCantLoseLifeCantPayLife() {
        gd.playerLifeTotals.put(player1Id, 20);
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPaysLife(3);
        StackEntry entry = counterEntry(effect);
        payerIsTheSpellsController(entry, effect);
        when(amountEvaluationService.evaluate(eq(gd), any(), any())).thenReturn(3);
        when(gameQueryService.canPlayerLoseLife(gd, player1Id)).thenReturn(false);

        assertThat(handler.cannotBePaid(gd, entry, effect)).isTrue();
    }

    @Test
    @DisplayName("An {X} ransom is the X announced for the countering spell or ability")
    void xRansomReadsTheAnnouncedX() {
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPaysX();
        StackEntry entry = counterEntry(effect);
        entry.setXValue(2);

        UnlessPaysEffectHandler.Ransom ransom = handler.ransom(gd, entry, effect);

        assertThat(ransom.manaCost()).isEqualTo("{2}");
        assertThat(ransom.xCost()).isTrue();
        assertThat(ransom.text()).isEqualTo("{2}");
    }

    @Test
    @DisplayName("A dynamic ransom reads the source as it last existed once it was sacrificed")
    void dynamicRansomReadsTheSourceSnapshot() {
        Card refrain = new Card();
        refrain.setName("Lilting Refrain");
        Permanent source = new Permanent(refrain);
        source.setCounterCount(CounterType.VERSE, 2);
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPays(new CountersOnSource(CounterType.VERSE));
        StackEntry entry = counterEntry(effect);
        entry.setSourcePermanentSnapshot(source);
        when(amountEvaluationService.evaluate(eq(gd), eq(new CountersOnSource(CounterType.VERSE)), any()))
                .thenReturn(2);

        UnlessPaysEffectHandler.Ransom ransom = handler.ransom(gd, entry, effect);

        assertThat(ransom.manaCost()).isEqualTo("{2}");
        verify(amountEvaluationService).evaluate(eq(gd), eq(new CountersOnSource(CounterType.VERSE)),
                argThat(context -> context.sourcePermanent() == source));
    }

    @Test
    @DisplayName("Mana and life together read as one cost")
    void manaAndLifeReadAsOneCost() {
        UnlessPaysEffect effect = UnlessPaysEffect.counterUnlessPays(2, 2);

        UnlessPaysEffectHandler.Ransom ransom = handler.ransom(gd, counterEntry(effect), effect);

        assertThat(ransom.text()).isEqualTo("{2} and 2 life");
    }
}
