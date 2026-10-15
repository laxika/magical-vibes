package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LazotepQuarry.class, GrizzlyBears.class, AirElemental.class})
class LazotepQuarryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorless() {
        Permanent quarry = addReadyQuarry();

        harness.activateAbility(player1, battlefieldIndex(quarry), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(quarry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a creature adds one mana of a chosen color")
    void sacrificesCreatureForAnyColorMana() {
        Permanent quarry = addReadyQuarry();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(quarry), 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sorcery ability exiles a matching creature and creates a 4/4 black Zombie copy")
    void createsZombieCopyOfCreatureWithManaValueX() {
        Permanent quarry = addReadyQuarry();
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, battlefieldIndex(quarry), 2, 2, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(quarry.getId()));
    }

    @Test
    @DisplayName("Cannot target a creature whose mana value differs from X")
    void rejectsWrongManaValueTarget() {
        Permanent quarry = addReadyQuarry();
        AirElemental target = new AirElemental();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(quarry), 2, 2, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value X");

        assertThat(quarry.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Mana value ability can only be activated at sorcery speed")
    void copyAbilityOnlyAtSorcerySpeed() {
        Permanent quarry = addReadyQuarry();
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(quarry), 2, 2, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(quarry.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Zombie copy replaces the original creature types")
    void zombieCopyReplacesOriginalCreatureTypes() {
        Permanent quarry = addReadyQuarry();
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, battlefieldIndex(quarry), 2, 2, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
                });
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void rejectsOpponentsGraveyard() {
        Permanent quarry = addReadyQuarry();
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(quarry), 2, 2, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(quarry.isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot copy a noncreature card even when its mana value matches X")
    void rejectsNoncreatureTarget() {
        Permanent quarry = addReadyQuarry();
        LazotepQuarry target = new LazotepQuarry();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(quarry), 2, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(quarry.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Lazotep Quarry");
    }

    @Test
    @DisplayName("Cannot produce colored mana without a creature to sacrifice")
    void coloredManaRequiresCreatureSacrifice() {
        Permanent quarry = addReadyQuarry();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(quarry), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(quarry.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Copy retains flying and enters untapped")
    void copyRetainsFlying() {
        Permanent quarry = addReadyQuarry();
        AirElemental target = new AirElemental();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, battlefieldIndex(quarry), 2, 5, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
                    assertThat(token.isTapped()).isFalse();
                });
        harness.assertNotInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("No token is created when the target leaves the graveyard before resolution")
    void targetLeavingGraveyardPreventsCopy() {
        Permanent quarry = addReadyQuarry();
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, battlefieldIndex(quarry), 2, 2, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Lazotep Quarry");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private Permanent addReadyQuarry() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent quarry = harness.addToBattlefieldAndReturn(player1, new LazotepQuarry());
        quarry.setSummoningSick(false);
        harness.clearPriorityPassed();
        return quarry;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
