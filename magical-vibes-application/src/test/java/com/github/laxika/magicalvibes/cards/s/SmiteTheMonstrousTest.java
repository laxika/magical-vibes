package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HystericalBlindness;
import com.github.laxika.magicalvibes.cards.m.MomentOfHeroism;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({SmiteTheMonstrous.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class,
        AbbeyGriffin.class, MomentOfHeroism.class, HystericalBlindness.class})
class SmiteTheMonstrousTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Smite the Monstrous targeting a creature with power 4+ puts it on stack")
    void castingPutsOnStack() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, wurm.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(SmiteTheMonstrous.class);
        assertThat(entry.getTargetId()).isEqualTo(wurm.getId());
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetSmallCreature() {
        // Add a valid target so spell is playable
        harness.addToBattlefield(player1, new CrawWurm());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Cannot target a creature with exactly power 3")
    void cannotTargetPower3Creature() {
        // Add a valid target so spell is playable
        harness.addToBattlefield(player1, new CrawWurm());

        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Resolving Smite the Monstrous destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Craw Wurm");
        harness.assertInGraveyard(player2, "Craw Wurm");
        harness.assertInGraveyard(player1, "Smite the Monstrous");
    }

    @Test
    @DisplayName("Smite the Monstrous allows regeneration")
    void allowsRegeneration() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        wurm.setRegenerationShield(1);

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Craw Wurm");
    }

    @Test
    @DisplayName("Smite the Monstrous fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, wurm.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Smite the Monstrous");
    }

    @Test
    @DisplayName("Can destroy your own creature whose boosted power is exactly four")
    void destroysOwnCreatureWithExactlyFourEffectivePower() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new AbbeyGriffin());
        harness.setHand(player1, List.of(new MomentOfHeroism(), new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, griffin.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(4);

        harness.castInstant(player1, 0, griffin.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Abbey Griffin");
        harness.assertInGraveyard(player1, "Abbey Griffin");
        harness.assertInGraveyard(player1, "Smite the Monstrous");
    }

    @Test
    @DisplayName("Does not destroy a target whose power falls below four before resolution")
    void fizzlesWhenTargetPowerDropsBelowFour() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new AbbeyGriffin());
        harness.setHand(player1, List.of(new MomentOfHeroism(), new SmiteTheMonstrous(), new HystericalBlindness()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstant(player1, 0, griffin.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(4);

        harness.castInstant(player1, 0, griffin.getId());
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, griffin)).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Abbey Griffin");
        harness.assertNotInGraveyard(player2, "Abbey Griffin");
        harness.assertInGraveyard(player1, "Smite the Monstrous");
        assertThat(gd.stack).isEmpty();
    }
}
