package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkirsdagFlayerTest extends BaseCardTest {

    // ===== Ability structure =====

    @Test
    @DisplayName("Has {3}{B}, {T}, Sacrifice a Human: destroy target creature activated ability")
    void hasCorrectAbility() {
        SkirsdagFlayer card = new SkirsdagFlayer();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{3}{B}");
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isEqualTo(new SacrificePermanentCost(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)
                )),
                "Sacrifice a Human",
                false
        ));
        assertThat(ability.getEffects().get(1)).isInstanceOf(DestroyTargetPermanentEffect.class);
        assertThat(ability.getTargetFilter()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));
    }

    // ===== Sacrifice a Human, destroy target creature =====

    @Test
    @DisplayName("Sacrificing a Human destroys target creature and taps the Flayer")
    void sacrificeHumanDestroysTargetCreature() {
        Permanent flayer = addReadyFlayer(player1);
        Permanent human = harness.addToBattlefieldAndReturn(player1, createHumanToken());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        addAbilityMana(player1);

        int flayerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(flayer);
        harness.activateAbility(player1, flayerIdx, null, bears.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, human.getId());
        harness.passBothPriorities();

        assertThat(flayer.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Human"));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(flayer);
    }

    @Test
    @DisplayName("Can sacrifice the Flayer itself when it is the only Human")
    void canSacrificeItselfWhenOnlyHuman() {
        Permanent flayer = addReadyFlayer(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        addAbilityMana(player1);

        int flayerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(flayer);
        harness.activateAbility(player1, flayerIdx, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Skirsdag Flayer"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Skirsdag Flayer"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void abilityRequiresMana() {
        addReadyFlayer(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        // Missing {3} generic mana

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadyFlayer(player1);
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = findPermanent(player2, "Forest");

        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyFlayer(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, bears.getId());
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Skirsdag Flayer"));
    }

    // ===== Helpers =====

    private Permanent addReadyFlayer(Player player) {
        SkirsdagFlayer card = new SkirsdagFlayer();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addAbilityMana(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.BLACK, 1);
    }

    private Card createHumanToken() {
        Card card = new Card();
        card.setName("Human");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.HUMAN));
        return card;
    }
}
