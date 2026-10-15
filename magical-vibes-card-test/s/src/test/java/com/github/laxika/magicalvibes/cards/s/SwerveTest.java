package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Swerve.class, Boomerang.class, CounselOfTheSoratami.class, GrizzlyBears.class,
        Incinerate.class, ProdigalPyromancer.class})
class SwerveTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Swerve requires targeting a spell with a single target")
    void castingRequiresSingleTargetSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Swerve()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, counsel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    @Test
    @DisplayName("Resolving Swerve retargets a single-target spell")
    void resolvingRetargetsSpell() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bears1);
        harness.addToBattlefield(player2, bears2);
        UUID bears1PermId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID bears2PermId = harness.getPermanentId(player2, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Swerve()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears1PermId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, boomerang.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(bears2PermId);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).doesNotContain(bears1PermId);

        harness.handlePermanentChosen(player2, bears2PermId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Swerve does nothing if there is no legal new target")
    void doesNothingWithoutAlternativeTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Swerve()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, boomerang.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        StackEntry boomerangEntry = gd.stack.getLast();
        assertThat(boomerangEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(boomerangEntry.getTargetId()).isEqualTo(bearsPermId);
    }

    @Test
    void cannotTargetSingleTargetActivatedAbility() {
        harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer()).setSummoningSick(false);
        harness.setHand(player2, List.of(new Swerve()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, abilityId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void retargetsDamageFromOpponentToSpellController() {
        Incinerate incinerate = new Incinerate();
        harness.setHand(player1, List.of(incinerate));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Swerve()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, incinerate.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId()).doesNotContain(player2.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }
}
