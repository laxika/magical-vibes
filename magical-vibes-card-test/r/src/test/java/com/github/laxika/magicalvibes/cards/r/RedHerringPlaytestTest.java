package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedHerringPlaytest.class, GrizzlyBears.class, Counterspell.class, Negate.class,
        Twincast.class, GiantGrowth.class, HolyStrength.class})
class RedHerringPlaytestTest extends BaseCardTest {

    @Test
    void exchangesWithAControlledPermanentWithoutResettingItsState() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        UUID targetId = target.getId();
        Card exchangedCard = target.getOriginalCard();
        RedHerringPlaytest redHerring = new RedHerringPlaytest();
        harness.setHand(player1, List.of(redHerring));
        addExchangeMana();

        harness.activateHandAbility(player1, 0, null);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).contains(redHerring);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        assertThat(target.getId()).isEqualTo(targetId);
        assertThat(target.getCard().getName()).isEqualTo("Red Herring");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(exchangedCard).doesNotContain(redHerring);
    }

    @Test
    void exchangesWithAControlledSpellAndTheNewSpellResolvesAsRedHerring() {
        GrizzlyBears spellCard = new GrizzlyBears();
        harness.castFromHand(player1, spellCard, "{1}{G}");
        StackEntry spell = gd.stack.getLast();
        UUID oldSpellId = spell.getTargetableId();

        RedHerringPlaytest redHerring = new RedHerringPlaytest();
        harness.setHand(player1, List.of(redHerring));
        addExchangeMana();
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, oldSpellId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Red Herring");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void doesNothingWhenOnlyTheOpponentControlsAPermanent() {
        Permanent opponentPermanent = addCreatureReady(player2, new GrizzlyBears());
        RedHerringPlaytest redHerring = new RedHerringPlaytest();
        harness.setHand(player1, List.of(redHerring));
        addExchangeMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(redHerring);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentPermanent);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void preservesAttachedAurasAndContinuousEffectsOnTheExchangedPermanent() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Holy Strength");

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        int powerBeforeExchange = gqs.getEffectivePower(gd, bears);
        int toughnessBeforeExchange = gqs.getEffectiveToughness(gd, bears);

        harness.setHand(player1, List.of(new RedHerringPlaytest()));
        addExchangeMana();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertOnBattlefield(player1, "Red Herring");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura, bears);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(powerBeforeExchange);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(toughnessBeforeExchange);
    }

    @Test
    void doesNothingWhenRedHerringHasLeftTheHandBeforeResolution() {
        Permanent permanent = addCreatureReady(player1, new GrizzlyBears());
        Card originalCard = permanent.getOriginalCard();
        harness.setHand(player1, List.of(new RedHerringPlaytest()));
        addExchangeMana();

        harness.activateHandAbility(player1, 0, null);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(permanent.getOriginalCard()).isSameAs(originalCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void incomingCounterspellCountersTheExchangedCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        RedHerringPlaytest redHerring = new RedHerringPlaytest();
        harness.setHand(player1, List.of(redHerring));
        addExchangeMana();
        harness.ensurePriority(player1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Red Herring");
        harness.assertNotOnBattlefield(player1, "Red Herring");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotChangeNegatesTargetToAnIllegalCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player2, bears, "{1}{G}");
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(counterspell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, bears.getId());

        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, counterspell.getId());
        StackEntry negate = gd.stack.getLast();

        RedHerringPlaytest redHerring = new RedHerringPlaytest();
        harness.setHand(player1, List.of(redHerring));
        addExchangeMana();
        harness.ensurePriority(player1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(negate.getTargetId()).isEqualTo(counterspell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Red Herring");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Counterspell");
    }

    @Test
    void canChooseACopiedSpellForTheExchange() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        harness.setHand(player1, List.of(new Twincast()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, counterspell.getId());
        harness.handleMayAbilityChosen(player1, false);
        UUID copyId = gd.stack.getLast().getTargetableId();

        RedHerringPlaytest redHerring = new RedHerringPlaytest();
        harness.setHand(player1, List.of(redHerring));
        addExchangeMana();
        harness.ensurePriority(player1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(copyId);
        harness.handlePermanentChosen(player1, copyId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Red Herring");
        harness.assertNotInHand(player1, "Counterspell");
    }

    private void addExchangeMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
