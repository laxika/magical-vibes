package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlummetTest extends BaseCardTest {

    @Test
    @DisplayName("Plummet has correct card properties")
    void hasCorrectProperties() {
        Plummet card = new Plummet();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getTargetFilter()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING)
                )),
                "Target must be a creature with flying"
        ));
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DestroyTargetPermanentEffect.class);
        DestroyTargetPermanentEffect effect = (DestroyTargetPermanentEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.cannotBeRegenerated()).isFalse();
    }

    @Test
    @DisplayName("Casting Plummet targeting a creature with flying puts it on stack")
    void castingPutsOnStack() {
        Permanent airElemental = new Permanent(new AirElemental());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(airElemental);

        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, airElemental.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Plummet");
        assertThat(entry.getTargetPermanentId()).isEqualTo(airElemental.getId());
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        // Add a creature with flying as valid target so spell is playable
        harness.getGameData().playerBattlefields.get(player1.getId()).add(new Permanent(new AirElemental()));

        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Resolving Plummet destroys target creature with flying")
    void resolvingDestroysTargetCreature() {
        Permanent airElemental = new Permanent(new AirElemental());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(airElemental);

        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, airElemental.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Air Elemental"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Air Elemental"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plummet"));
    }

    @Test
    @DisplayName("Plummet fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent airElemental = new Permanent(new AirElemental());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(airElemental);

        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, airElemental.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plummet"));
    }
}
