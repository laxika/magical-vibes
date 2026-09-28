package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Index;
import com.github.laxika.magicalvibes.cards.m.MasterDecoy;
import com.github.laxika.magicalvibes.cards.p.Pyrotechnics;
import com.github.laxika.magicalvibes.cards.v.VolcanicHammer;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Deflection.class, Boomerang.class, Index.class, Pyrotechnics.class,
        GrizzlyBears.class, MasterDecoy.class, VolcanicHammer.class, Zombify.class})
class DeflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Deflection requires targeting a spell with a single target")
    void castingRequiresSingleTargetSpell() {
        Index index = new Index();
        harness.setHand(player1, List.of(index));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Deflection()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, index.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    @Test
    @DisplayName("Casting Deflection cannot target a spell with multiple targets")
    void castingRejectsMultiTargetSpell() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        UUID bears1PermId = harness.addToBattlefieldAndReturn(player1, bears1).getId();
        UUID bears2PermId = harness.addToBattlefieldAndReturn(player2, bears2).getId();

        Pyrotechnics pyrotechnics = new Pyrotechnics();
        harness.setHand(player1, List.of(pyrotechnics));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, Map.of(bears1PermId, 2, bears2PermId, 2));
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Deflection()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, pyrotechnics.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    @Test
    @DisplayName("Casting Deflection can target a variable-target spell cast with one target")
    void castingAcceptsVariableTargetSpellWithOneTarget() {
        Pyrotechnics pyrotechnics = new Pyrotechnics();
        harness.setHand(player1, List.of(pyrotechnics));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, Map.of(player2.getId(), 4));
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Deflection()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, pyrotechnics.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casting Deflection cannot target a single-target activated ability")
    void castingRejectsActivatedAbility() {
        MasterDecoy masterDecoy = new MasterDecoy();
        GrizzlyBears bears = new GrizzlyBears();
        addCreatureReady(player1, masterDecoy);
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, bears).getId();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, bearsPermId);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Deflection()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, masterDecoy.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell");
    }

    @Test
    @DisplayName("Resolving Deflection retargets a single-target spell")
    void resolvingRetargetsSpell() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        UUID bears1PermId = harness.addToBattlefieldAndReturn(player1, bears1).getId();
        UUID bears2PermId = harness.addToBattlefieldAndReturn(player2, bears2).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Deflection()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, bears1PermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(bears2PermId);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).doesNotContain(bears1PermId);

        harness.handlePermanentChosen(player2, bears2PermId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears2PermId));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears1PermId));
    }

    @Test
    @DisplayName("Deflection can retarget a spell after its original target leaves the battlefield")
    void canRetargetAfterOriginalTargetLeavesBattlefield() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        UUID bears1PermId = harness.addToBattlefieldAndReturn(player1, bears1).getId();
        UUID bears2PermId = harness.addToBattlefieldAndReturn(player2, bears2).getId();

        Boomerang firstBoomerang = new Boomerang();
        Boomerang secondBoomerang = new Boomerang();
        harness.setHand(player1, List.of(firstBoomerang, secondBoomerang));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setHand(player2, List.of(new Deflection()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, bears1PermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, firstBoomerang.getId());
        harness.castInstant(player1, 0, bears1PermId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears2PermId)
                .doesNotContain(bears1PermId);

        harness.handlePermanentChosen(player2, bears2PermId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears1PermId));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears2PermId));
    }

    @Test
    @DisplayName("Deflection can retarget a spell targeting a creature card in a graveyard")
    void canRetargetGraveyardTargetSpell() {
        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstBears, secondBears));

        Zombify zombify = new Zombify();
        harness.setHand(player1, List.of(zombify));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.setHand(player2, List.of(new Deflection()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, firstBears.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, zombify.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(secondBears.getId())
                .doesNotContain(firstBears.getId());

        harness.handlePermanentChosen(player2, secondBears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(secondBears.getId()))
                .noneMatch(p -> p.getCard().getId().equals(firstBears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstBears)
                .doesNotContain(secondBears);
    }

    @Test
    @DisplayName("Deflection does nothing if there is no legal new target")
    void doesNothingWithoutAlternativeTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, bears).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Deflection()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        StackEntry boomerangEntry = gd.stack.getLast();
        assertThat(boomerangEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(boomerangEntry.getTargetId()).isEqualTo(bearsPermId);
    }

    @Test
    @DisplayName("Deflection can retarget a player-target spell to another legal player")
    void canRetargetPlayerTargetSpell() {
        VolcanicHammer volcanicHammer = new VolcanicHammer();
        harness.setHand(player1, List.of(volcanicHammer));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Deflection()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        GameData gd = harness.getGameData();
        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, volcanicHammer.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).doesNotContain(player2.getId());

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, p1LifeBefore - 3);
        harness.assertLife(player2, p2LifeBefore);
    }
}
