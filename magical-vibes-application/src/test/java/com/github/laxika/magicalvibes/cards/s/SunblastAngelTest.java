package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SunblastAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Sunblast Angel has correct ETB effect")
    void hasCorrectProperties() {
        SunblastAngel card = new SunblastAngel();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(DestroyAllPermanentsEffect.class);
        DestroyAllPermanentsEffect effect = (DestroyAllPermanentsEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.filter()).isInstanceOf(PermanentAllOfPredicate.class);
        assertThat(effect.cannotBeRegenerated()).isFalse();
    }

    @Test
    @DisplayName("ETB destroys tapped creatures on both sides")
    void etbDestroysTappedCreaturesOnBothSides() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        // Tap both creatures
        Permanent bears = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        bears.tap();

        Permanent elves = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();
        elves.tap();

        harness.setHand(player1, List.of(new SunblastAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        // Resolve creature spell → ETB triggers
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("ETB does not destroy untapped creatures")
    void etbDoesNotDestroyUntappedCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SunblastAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        // Resolve creature spell → ETB triggers
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Sunblast Angel itself is untapped so does not destroy itself")
    void doesNotDestroyItself() {
        harness.setHand(player1, List.of(new SunblastAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        // Resolve creature spell → ETB triggers
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sunblast Angel"));
    }

    @Test
    @DisplayName("Indestructible tapped creature survives ETB")
    void indestructibleTappedCreatureSurvives() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        bears.tap();
        bears.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.setHand(player1, List.of(new SunblastAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        // Resolve creature spell → ETB triggers
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Tapped creature with regeneration shield can be regenerated")
    void tappedCreatureCanBeRegenerated() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        bears.tap();
        bears.setRegenerationShield(1);

        harness.setHand(player1, List.of(new SunblastAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        // Resolve creature spell → ETB triggers
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature should survive via regeneration since cannotBeRegenerated is false
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
