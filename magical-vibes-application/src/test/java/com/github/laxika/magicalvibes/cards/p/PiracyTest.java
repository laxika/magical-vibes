package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AncientTomb;
import com.github.laxika.magicalvibes.cards.c.CudgelTroll;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Piracy.class, AncientTomb.class, CudgelTroll.class, Forest.class, GrizzlyBears.class})
class PiracyTest extends BaseCardTest {

    private void castPiracy() {
        harness.castFromHand(player1, new Piracy(), "{U}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting Piracy lets its controller tap lands they don't control")
    void grantsForeignTapPermission() {
        castPiracy();

        assertThat(gd.mayTapLandsForSpellsUntilEndOfTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("Tapping an opponent's land produces spell-only mana in your pool")
    void tapsOpponentLandForSpellOnlyMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        castPiracy();

        harness.tapForeignLandForMana(player1, forest.getId());

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(forest.isTapped()).isTrue();
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.getSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Piracy can tap an opponent's land with an activated mana ability")
    void tapsOpponentLandWithActivatedManaAbility() {
        harness.setLife(player1, 20);
        Permanent ancientTomb = harness.addToBattlefieldAndReturn(player2, new AncientTomb());
        castPiracy();

        harness.tapForeignLandForMana(player1, ancientTomb.getId());

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(ancientTomb.isTapped()).isTrue();
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(pool.getSpellOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Spell-only mana can be spent to cast a spell")
    void spellOnlyManaCastsASpell() {
        Permanent forest1 = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent forest2 = harness.addToBattlefieldAndReturn(player2, new Forest());
        castPiracy();

        harness.tapForeignLandForMana(player1, forest1.getId());
        harness.tapForeignLandForMana(player1, forest2.getId());

        // Grizzly Bears costs {1}{G}; both green mana comes from the tapped opponent Forests
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(0);
    }

    @Test
    @DisplayName("Spell-only mana cannot pay an activated ability's cost")
    void spellOnlyManaCannotPayActivatedAbility() {
        Permanent troll = addCreatureReady(player1, new CudgelTroll());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        castPiracy();

        harness.tapForeignLandForMana(player1, forest.getId());

        // Cudgel Troll's "{G}: Regenerate" cannot be paid with Piracy's spell-only mana
        int trollIndex = gd.playerBattlefields.get(player1.getId()).indexOf(troll);
        assertThatThrownBy(() -> harness.activateAbility(player1, trollIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        // The spell-only mana is left untouched after the failed activation
        assertThat(gd.playerManaPools.get(player1.getId()).getSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot tap a land you don't control without Piracy")
    void cannotTapForeignLandWithoutPiracy() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.tapForeignLandForMana(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Piracy does not let you tap your own lands via the foreign-tap action")
    void cannotTapOwnLandViaForeignTap() {
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castPiracy();

        assertThatThrownBy(() -> harness.tapForeignLandForMana(player1, ownForest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Piracy only grants foreign-land permission to its controller")
    void onlyControllerCanTapForeignLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castPiracy();

        assertThatThrownBy(() -> harness.tapForeignLandForMana(player2, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Permission to tap foreign lands wears off at end of turn")
    void permissionClearedAtEndOfTurn() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        castPiracy();

        assertThat(gd.mayTapLandsForSpellsUntilEndOfTurn).isNotEmpty();

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThatThrownBy(() -> harness.tapForeignLandForMana(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
