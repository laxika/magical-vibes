package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.r.RustGoliath;
import com.github.laxika.magicalvibes.cards.s.SaheeliFiligreeMaster;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExcavationExplosion.class, RustGoliath.class, SaheeliFiligreeMaster.class})
class ExcavationExplosionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a creature and creates a tapped Powerstone")
    void dealsDamageToCreatureAndCreatesPowerstone() {
        harness.addToBattlefield(player2, new RustGoliath());
        harness.setHand(player1, List.of(new ExcavationExplosion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Rust Goliath"));

        assertThat(findPermanent(player2, "Rust Goliath").getMarkedDamage()).isEqualTo(3);
        Permanent powerstone = findPermanent(player1, "Powerstone");
        assertThat(powerstone.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(powerstone.getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
        assertThat(powerstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 3 damage to a player and creates a Powerstone")
    void dealsDamageToPlayerAndCreatesPowerstone() {
        harness.setHand(player1, List.of(new ExcavationExplosion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
    }

    @Test
    void canTargetItsController() {
        harness.setHand(player1, List.of(new ExcavationExplosion()));
        harness.addMana(player1, ManaColor.RED, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, lifeBefore - 3);
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    @Test
    void dealsDamageToPlaneswalkerAndStillCreatesPowerstone() {
        Permanent saheeli = harness.addToBattlefieldAndReturn(player2, new SaheeliFiligreeMaster());
        saheeli.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new ExcavationExplosion()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, saheeli.getId());

        harness.assertNotOnBattlefield(player2, "Saheeli, Filigree Master");
        harness.assertInGraveyard(player2, "Saheeli, Filigree Master");
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
    }

    @Test
    void doesNotCreatePowerstoneWhenOnlyTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RustGoliath());
        harness.setHand(player1, List.of(new ExcavationExplosion()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
        harness.assertInGraveyard(player1, "Excavation Explosion");
        harness.assertInHand(player2, "Rust Goliath");
    }

    @Test
    void powerstoneManaCanPayForArtifactSpell() {
        harness.setHand(player1, List.of(new ExcavationExplosion()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        Permanent powerstone = findPermanent(player1, "Powerstone");
        powerstone.setTapped(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(powerstone), null, null);
        assertThat(powerstone.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        harness.setHand(player1, List.of(new RustGoliath()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rust Goliath");
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    @Test
    void powerstoneManaCannotPayForNonartifactSpell() {
        harness.setHand(player1, List.of(new ExcavationExplosion()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        Permanent powerstone = findPermanent(player1, "Powerstone");
        powerstone.setTapped(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(powerstone), null, null);
        harness.setHand(player1, List.of(new ExcavationExplosion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Excavation Explosion");
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
    }
}
