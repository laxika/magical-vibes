package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DoomBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Doom Blade has correct card properties")
    void hasCorrectProperties() {
        DoomBlade card = new DoomBlade();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getTargetFilter()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
                )),
                "Target must be a nonblack creature"
        ));
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DestroyTargetPermanentEffect.class);
        DestroyTargetPermanentEffect effect = (DestroyTargetPermanentEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.cannotBeRegenerated()).isFalse();
    }

    @Test
    @DisplayName("Casting Doom Blade targeting a nonblack creature puts it on stack")
    void castingPutsOnStack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Doom Blade");
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        // Add a nonblack creature as valid target so spell is playable
        harness.getGameData().playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));

        Permanent blackCreature = new Permanent(new MassOfGhouls());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blackCreature);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Can target an artifact creature (unlike Terror)")
    void canTargetArtifactCreature() {
        Permanent artifactCreature = new Permanent(new BottleGnomes());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(artifactCreature);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, artifactCreature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(artifactCreature.getId());
    }

    @Test
    @DisplayName("Resolving Doom Blade destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Doom Blade"));
    }

    @Test
    @DisplayName("Doom Blade fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Doom Blade"));
    }
}
