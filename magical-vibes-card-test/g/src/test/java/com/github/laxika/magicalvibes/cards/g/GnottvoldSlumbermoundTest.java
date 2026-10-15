package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GnottvoldSlumbermound.class, Forest.class, GnottvoldRecluse.class})
class GnottvoldSlumbermoundTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for red mana")
    void entersTappedAndTapsForRedMana() {
        harness.setHand(player1, List.of(new GnottvoldSlumbermound()));
        harness.playLand(player1, 0);
        Permanent land = findPermanent(player1, "Gnottvold Slumbermound");

        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifices itself, destroys a target land, and creates a trampling Troll Warrior")
    void sacrificesAndCreatesTrollWarrior() {
        addManaForAbility();
        harness.addToBattlefield(player1, new GnottvoldSlumbermound());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gnottvold Slumbermound");
        harness.assertInGraveyard(player1, "Gnottvold Slumbermound");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.TROLL, CardSubtype.WARRIOR);
        assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        addManaForAbility();
        harness.addToBattlefield(player1, new GnottvoldSlumbermound());
        Permanent target = addCreatureReady(player2, new GnottvoldRecluse());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy another land controlled by its controller")
    void canTargetOwnLand() {
        addManaForAbility();
        harness.addToBattlefield(player1, new GnottvoldSlumbermound());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.assertInGraveyard(player1, "Gnottvold Slumbermound");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allMatch(p -> p.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Targeting itself sacrifices the source but creates no token")
    void targetingItselfCreatesNoToken() {
        addManaForAbility();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GnottvoldSlumbermound());

        harness.activateAbility(player1, 0, 1, null, source.getId());
        harness.assertInGraveyard(player1, "Gnottvold Slumbermound");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creates no token when the target leaves before resolution")
    void missingTargetCreatesNoToken() {
        addManaForAbility();
        harness.addToBattlefield(player1, new GnottvoldSlumbermound());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gnottvold Slumbermound");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped source cannot activate the land destruction ability")
    void tappedSourceCannotActivate() {
        addManaForAbility();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GnottvoldSlumbermound());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        source.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Gnottvold Slumbermound");
        harness.assertOnBattlefield(player2, "Forest");
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
