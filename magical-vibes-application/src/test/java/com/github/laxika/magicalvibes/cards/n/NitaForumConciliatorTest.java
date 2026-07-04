package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NitaForumConciliatorTest extends BaseCardTest {

    private void mainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    // ===== Structure =====

    @Test
    @DisplayName("Has spell-cast trigger and the exile-cast activated ability")
    void hasCorrectAbilities() {
        NitaForumConciliator nita = new NitaForumConciliator();

        assertThat(nita.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(nita.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);

        assertThat(nita.getActivatedAbilities()).hasSize(1);
        assertThat(nita.getActivatedAbilities().getFirst().getEffects())
                .anyMatch(e -> e instanceof SacrificeCreatureCost)
                .anyMatch(e -> e instanceof ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect);
    }

    // ===== Ability 1: cast a spell you don't own =====

    @Test
    @DisplayName("Casting a spell you don't own puts a +1/+1 counter on each creature you control")
    void castingUnownedSpellAddsCounters() {
        mainPhase();
        Permanent nita = harness.addToBattlefieldAndReturn(player1, new NitaForumConciliator());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        Divination divination = new Divination();
        divination.setOwnerId(player2.getId()); // a spell you don't own
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve trigger, then Divination

        assertThat(nita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell you own does not add counters")
    void castingOwnedSpellAddsNoCounters() {
        mainPhase();
        Permanent nita = harness.addToBattlefieldAndReturn(player1, new NitaForumConciliator());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        Divination divination = new Divination();
        divination.setOwnerId(player1.getId()); // a spell you own
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(nita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    // ===== Ability 2: exile target instant/sorcery from an opponent's graveyard =====

    @Test
    @DisplayName("Activating exiles the opponent's instant/sorcery and grants this-turn cast permission")
    void activatingExilesAndGrantsPermission() {
        mainPhase();
        harness.addToBattlefield(player1, new NitaForumConciliator());
        harness.addToBattlefield(player1, new GrizzlyBears()); // sacrifice fodder
        harness.addMana(player1, ManaColor.WHITE, 2);

        Divination target = new Divination();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities(); // resolve the ability

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target);
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(target.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(target.getId());
        assertThat(gd.exilePlayAnyManaType).contains(target.getId());
        assertThat(gd.exileInsteadOfGraveyard).contains(target.getId());
        // The other creature was sacrificed to pay the cost.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The exiled spell can be cast this turn with mana of any type and is exiled, not put into a graveyard")
    void exiledSpellCastWithAnyManaThenExiled() {
        mainPhase();
        harness.addToBattlefield(player1, new NitaForumConciliator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2); // for the ability
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        Divination target = new Divination(); // costs {2}{U}
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Pay {2}{U} entirely with green mana — only legal because "mana of any type" is granted.
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, target.getId());
        harness.passBothPriorities(); // resolve Divination

        // Drew 2 cards from the 2-card library.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        // Exiled instead of being put into a graveyard.
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target);
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
    }

    @Test
    @DisplayName("This-turn cast riders are cleared during cleanup")
    void ridersClearedAtCleanup() {
        mainPhase();
        harness.addToBattlefield(player1, new NitaForumConciliator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        Divination target = new Divination();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        assertThat(gd.exilePlayAnyManaType).contains(target.getId());

        // Advance through end step to the cleanup step.
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step
        harness.passBothPriorities(); // advance to cleanup

        assertThat(gd.exilePlayPermissions).doesNotContainKey(target.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(target.getId());
        assertThat(gd.exileInsteadOfGraveyard).doesNotContain(target.getId());
    }

    // ===== Ability 2: illegal targets =====

    @Test
    @DisplayName("Cannot target a card in your own graveyard")
    void cannotTargetOwnGraveyard() {
        mainPhase();
        harness.addToBattlefield(player1, new NitaForumConciliator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        Divination ownCard = new Divination();
        harness.setGraveyard(player1, List.of(ownCard));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, 0, ownCard.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetCreatureCard() {
        mainPhase();
        harness.addToBattlefield(player1, new NitaForumConciliator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        GrizzlyBears creatureCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creatureCard));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, 0, creatureCard.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
