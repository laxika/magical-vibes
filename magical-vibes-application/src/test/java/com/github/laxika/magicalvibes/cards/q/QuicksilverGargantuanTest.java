package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.c.ChimericMass;
import com.github.laxika.magicalvibes.cards.o.OriginSpellbomb;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Nightmare;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuicksilverGargantuan.class, AirElemental.class, AngelOfMercy.class, Clone.class,
        GrizzlyBears.class, Nightmare.class, Swamp.class, Terror.class, ChimericMass.class,
        OriginSpellbomb.class})
class QuicksilverGargantuanTest extends BaseCardTest {

    @Test
    @DisplayName("Quicksilver Gargantuan copies a creature but has 7/7 power and toughness")
    void copiesCreatureButIs7x7() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        Permanent clonePerm = findPermanent(player1, "Grizzly Bears");

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(clonePerm.getCard().getPower()).isEqualTo(7);
        assertThat(clonePerm.getCard().getToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Quicksilver Gargantuan copies keywords from target creature")
    void copiesKeywords() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.handlePermanentChosen(player1, targetId);

        Permanent clonePerm = findPermanent(player1, "Air Elemental");

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getName()).isEqualTo("Air Elemental");
        assertThat(clonePerm.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(clonePerm.getCard().getPower()).isEqualTo(7);
        assertThat(clonePerm.getCard().getToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Quicksilver Gargantuan copies subtypes from target creature")
    void copiesSubtypes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        Permanent clonePerm = findPermanent(player1, "Grizzly Bears");

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getSubtypes()).containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Quicksilver Gargantuan copying a creature with ETB triggers that effect")
    void copiedCreatureETBFires() {
        harness.addToBattlefield(player2, new AngelOfMercy());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID angelId = harness.getPermanentId(player2, "Angel of Mercy");
        harness.handlePermanentChosen(player1, angelId);

        Permanent clonePerm = findPermanent(player1, "Angel of Mercy");
        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getName()).isEqualTo("Angel of Mercy");
        assertThat(clonePerm.getCard().getPower()).isEqualTo(7);
        assertThat(clonePerm.getCard().getToughness()).isEqualTo(7);

        // The copied Angel of Mercy's ETB "gain 3 life" should be on the stack
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getDescription().contains("Angel of Mercy"));

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Quicksilver Gargantuan survives as 7/7 when player declines to copy")
    void survivesWhenPlayerDeclines() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Decline to copy
        harness.handleMayAbilityChosen(player1, false);

        // Unlike Clone, Quicksilver Gargantuan is a 7/7 and should survive
        Permanent perm = findPermanent(player1, "Quicksilver Gargantuan");

        assertThat(perm).isNotNull();
        assertThat(perm.getCard().getPower()).isEqualTo(7);
        assertThat(perm.getCard().getToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Quicksilver Gargantuan survives as 7/7 when no creatures on battlefield")
    void survivesWhenNoCreatures() {
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // With no creatures to copy, it enters as its base 7/7 self
        Permanent perm = findPermanent(player1, "Quicksilver Gargantuan");

        assertThat(perm).isNotNull();
        assertThat(perm.getCard().getPower()).isEqualTo(7);
        assertThat(perm.getCard().getToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Quicksilver Gargantuan does not copy P/T characteristic-defining ability (CR 707.9d)")
    void doesNotCopyPTCharacteristicDefiningAbility() {
        // Nightmare has */* where * = number of Swamps you control
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Nightmare());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID nightmareId = harness.getPermanentId(player2, "Nightmare");
        harness.handlePermanentChosen(player1, nightmareId);

        Permanent clonePerm = findPermanent(player1, "Nightmare");

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getName()).isEqualTo("Nightmare");
        // Per CR 707.9d, the CDA is not copied — P/T should be exactly 7/7
        assertThat(clonePerm.getEffectivePower()).isEqualTo(7);
        assertThat(clonePerm.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Clone copying a Quicksilver Gargantuan copy gets the 7/7 exception (CR 707.2)")
    void cloneOfGargantuanCopyIsSevenSeven() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        Permanent gargantuan = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Clone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, gargantuan.getId());

        Permanent clonePerm = findPermanents(player1, "Grizzly Bears").stream()
                .filter(p -> !p.getId().equals(gargantuan.getId()))
                .findFirst().orElseThrow();

        // The copy exception is part of the Gargantuan's copiable values: the Clone is a
        // 7/7 Grizzly Bears, not a 2/2.
        assertThat(clonePerm.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(clonePerm.getCard().getPower()).isEqualTo(7);
        assertThat(clonePerm.getCard().getToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Quicksilver Gargantuan goes to graveyard as Quicksilver Gargantuan when destroyed")
    void goesToGraveyardAsOriginalCard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        Permanent clonePerm = findPermanent(player1, "Grizzly Bears");
        assertThat(clonePerm).isNotNull();

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, clonePerm.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.assertInGraveyard(player1, "Quicksilver Gargantuan");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Copying an animated artifact does not copy its animation or charge counters")
    void copiesAnimatedArtifactWithoutAnimationOrCounters() {
        harness.setHand(player1, List.of(new ChimericMass(), new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();
        Permanent mass = findPermanent(player1, "Chimeric Mass");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, mass)).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, mass.getId());

        Permanent copy = findPermanents(player1, "Chimeric Mass").stream()
                .filter(p -> !p.getId().equals(mass.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.isCreature(gd, copy)).isFalse();
        assertThat(copy.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(copy.isAnimatedUntilEndOfTurn()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Quicksilver Gargantuan");
        assertThat(findPermanents(player1, "Chimeric Mass")).containsExactly(mass);
    }

    @Test
    @DisplayName("Copying a token creates a nontoken 7/7 with its copiable characteristics")
    void copiesTokenWithoutBecomingAToken() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Myr");
        token.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        token.tap();

        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        Permanent copy = findPermanents(player1, "Myr").stream()
                .filter(p -> !p.getId().equals(token.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.getCard().isToken()).isFalse();
        assertThat(copy.getCard().getSubtypes()).containsExactly(CardSubtype.MYR);
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copy.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(7);
    }

    @Test
    @DisplayName("Copying an uncopied Gargantuan offers its acquired copy replacement before entry")
    void canApplyNewlyCopiedEnterReplacement() {
        harness.addToBattlefield(player2, new QuicksilverGargantuan());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new QuicksilverGargantuan()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1,
                harness.getPermanentId(player2, "Quicksilver Gargantuan"));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Air Elemental"));

        Permanent copy = findPermanent(player1, "Air Elemental");
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(7);
        assertThat(copy.getCard().getKeywords()).contains(Keyword.FLYING);
    }
}
