package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.b.BlazingTorch;
import com.github.laxika.magicalvibes.cards.r.RecklessWaif;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VictimOfNight.class, AmbushViper.class, VampireInterloper.class,
        RecklessWaif.class, WalkingCorpse.class, BlazingTorch.class})
class VictimOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Victim of Night targeting a valid creature puts it on stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AmbushViper());

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot target a Vampire creature")
    void cannotTargetVampire() {
        harness.addToBattlefield(player1, new AmbushViper());

        Permanent vampirePerm = harness.addToBattlefieldAndReturn(player2, new VampireInterloper());

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, vampirePerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Vampire");
    }

    @Test
    @DisplayName("Cannot target a Werewolf creature")
    void cannotTargetWerewolf() {
        harness.addToBattlefield(player1, new AmbushViper());

        Permanent werewolfPerm = harness.addToBattlefieldAndReturn(player2, new RecklessWaif());

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, werewolfPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Werewolf");
    }

    @Test
    @DisplayName("Cannot target a Zombie creature")
    void cannotTargetZombie() {
        harness.addToBattlefield(player1, new AmbushViper());

        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, zombie.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Zombie");
    }

    @Test
    @DisplayName("Resolving Victim of Night destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AmbushViper());

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Ambush Viper");
        harness.assertInGraveyard(player2, "Ambush Viper");
        harness.assertInGraveyard(player1, "Victim of Night");
    }

    @Test
    @DisplayName("Victim of Night allows regeneration")
    void allowsRegeneration() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AmbushViper());
        bears.setRegenerationShield(1);

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertOnBattlefield(player2, "Ambush Viper");
        harness.assertNotInGraveyard(player2, "Ambush Viper");
    }

    @Test
    @DisplayName("Victim of Night fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AmbushViper());

        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Victim of Night");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent torch = harness.addToBattlefieldAndReturn(player2, new BlazingTorch());
        harness.addToBattlefield(player2, new AmbushViper());
        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, torch.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Blazing Torch");
    }

    @Test
    @DisplayName("Can destroy a creature controlled by the caster")
    void destroysOwnCreature() {
        Permanent viper = harness.addToBattlefieldAndReturn(player1, new AmbushViper());
        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, viper.getId());

        harness.assertNotOnBattlefield(player1, "Ambush Viper");
        harness.assertInGraveyard(player1, "Ambush Viper");
        harness.assertInGraveyard(player1, "Victim of Night");
    }

    @Test
    @DisplayName("Target gaining a prohibited subtype before resolution is not destroyed")
    void fizzlesIfTargetBecomesZombie() {
        Permanent viper = harness.addToBattlefieldAndReturn(player2, new AmbushViper());
        harness.setHand(player1, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, viper.getId());

        viper.getGrantedSubtypes().add(CardSubtype.ZOMBIE);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ambush Viper");
        harness.assertNotInGraveyard(player2, "Ambush Viper");
        harness.assertInGraveyard(player1, "Victim of Night");
    }
}
