package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.a.ArcumsAstrolabe;
import com.github.laxika.magicalvibes.cards.c.Chillerpillar;
import com.github.laxika.magicalvibes.cards.e.EndlessOne;
import com.github.laxika.magicalvibes.cards.m.MagmaticSinkhole;
import com.github.laxika.magicalvibes.cards.r.RainOfRevelation;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import org.junit.jupiter.api.DisplayName;




@CardUsed({UrzaLordHighArtificer.class, MindStone.class, ArcumsAstrolabe.class,
        Chillerpillar.class, EndlessOne.class, MagmaticSinkhole.class, RainOfRevelation.class,
        SnowCoveredIsland.class})
class UrzaLordHighArtificerTest extends BaseCardTest {

    @Test
    void entersWithAConstructThatScalesWithArtifactsYouControl() {
        harness.addToBattlefield(player1, new MindStone());
        castUrza();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);

        harness.addToBattlefield(player1, new MindStone());

        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(3);
    }

    @Test
    void tapsAnArtifactToAddBlueMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        addReadyUrza();

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void shufflesAndExilesTheTopCardWithFreePlayPermission() {
        addReadyUrza();
        Card top = new RainOfRevelation();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(top.getId());
    }

    @Test
    void canCastTheExiledCardWithoutMana() {
        addReadyUrza();
        Card top = new RainOfRevelation();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        gd.playerManaPools.get(player1.getId()).clear();
        harness.castFromExile(player1, top.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(top.getId())
                && entry.getEntryType() == StackEntryType.INSTANT_SPELL);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void constructCountsItselfAndIgnoresOpponentsArtifacts() {
        harness.addToBattlefield(player2, new ArcumsAstrolabe());
        castUrza();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);
    }

    @Test
    void canTapTheNewlyCreatedConstructWhileUrzaIsSummoningSick() {
        castUrza();
        Permanent construct = findPermanent(player1, "Construct");

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(construct.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Urza, Lord High Artificer").isTapped()).isFalse();
    }

    @Test
    void cannotPayManaAbilityWithTappedOrOpponentsArtifacts() {
        addReadyUrza();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ArcumsAstrolabe());
        artifact.tap();
        harness.addToBattlefield(player2, new ArcumsAstrolabe());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void emptyLibraryDoesNotPreventAbilityResolving() {
        addReadyUrza();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canPlayExiledLandButCannotExceedLandPlayLimit() {
        addReadyUrza();
        Card firstLand = new SnowCoveredIsland();
        exileWithUrza(firstLand);
        harness.castFromExile(player1, firstLand.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(firstLand.getId()));
        assertThat(gd.stack).isEmpty();

        Card secondLand = new SnowCoveredIsland();
        exileWithUrza(secondLand);
        assertThatThrownBy(() -> harness.castFromExile(player1, secondLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(secondLand);
    }

    @Test
    void freeCreatureStillRequiresSorceryTiming() {
        addReadyUrza();
        Card creature = new Chillerpillar();
        exileWithUrza(creature);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
    }

    @Test
    void canCastDelveSpellForFreeWithoutExilingGraveyardCards() {
        addReadyUrza();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Chillerpillar());
        Card spell = new MagmaticSinkhole();
        exileWithUrza(spell);

        harness.castFromExile(player1, spell.getId(), target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotChooseNonzeroXForFreeSpell() {
        addReadyUrza();
        Card spell = new EndlessOne();
        exileWithUrza(spell);

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, spell.getId(), 5, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void unusedExiledCardRemainsExiledAfterPermissionExpires() {
        addReadyUrza();
        Card spell = new Chillerpillar();
        exileWithUrza(spell);
        harness.setLibrary(player1, List.of(new Chillerpillar(), new Chillerpillar()));
        harness.setLibrary(player2, List.of(new Chillerpillar(), new Chillerpillar()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(spell.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(spell.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void exileWithUrza(Card card) {
        harness.setLibrary(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }

    private void castUrza() {
        harness.setHand(player1, List.of(new UrzaLordHighArtificer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyUrza() {
        return addCreatureReady(player1, new UrzaLordHighArtificer());
    }
}

@CardUsed({UrzaLordHighArtificer.class, CopperMyr.class, GrizzlyBears.class})
class Mh1UrzaLordHighArtificerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Construct that scales with artifacts you control")
    void constructScalesWithControlledArtifacts() {
        Permanent firstArtifact = addCreatureReady(player1, new CopperMyr());
        harness.setHand(player1, java.util.List.of(new UrzaLordHighArtificer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);

        harness.addToBattlefield(player1, new CopperMyr());
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(firstArtifact);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping an artifact with the first ability adds blue mana")
    void tapsArtifactForBlueMana() {
        Permanent urza = addCreatureReady(player1, new UrzaLordHighArtificer());
        Permanent artifact = addCreatureReady(player1, new CopperMyr());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(urza), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability exiles the shuffled top card for free play")
    void exilesTopCardForFreePlay() {
        Permanent urza = addCreatureReady(player1, new UrzaLordHighArtificer());
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(urza), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(top.getId());

        gd.playerManaPools.get(player1.getId()).clear();
        harness.castFromExile(player1, top.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(top.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
