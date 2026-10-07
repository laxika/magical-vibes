package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RattleclawMystic;
import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.cards.r.RedHerring;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TinStreetGossip.class, RattleclawMystic.class, NervousGardener.class, RedHerring.class})
class TinStreetGossipTest extends BaseCardTest {

    @Test
    void restrictedManaCannotCastNormalSpell() {
        addCreatureReady(player1, new TinStreetGossip());
        harness.setHand(player1, List.of(new RattleclawMystic()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedManaCastsFaceDownSpellAndTurnsItFaceUp() {
        addCreatureReady(player1, new TinStreetGossip());
        addCreatureReady(player1, new TinStreetGossip());
        harness.setHand(player1, List.of(new RattleclawMystic()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent mystic = findPermanent(player1, "Rattleclaw Mystic");
        assertThat(mystic.isFaceDown()).isTrue();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mystic));

        assertThat(mystic.isFaceDown()).isFalse();
    }

    @Test
    void manaAbilityImmediatelyProducesOneRedAndOneGreenAndTapsSource() {
        Permanent gossip = addCreatureReady(player1, new TinStreetGossip());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gossip.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getFaceDownSpellsOrTurnFaceUpMana(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getFaceDownSpellsOrTurnFaceUpMana(ManaColor.GREEN)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summoningSickGossipCannotActivateTapAbility() {
        Permanent gossip = addCreatureReady(player1, new TinStreetGossip());
        gossip.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gossip.isTapped()).isFalse();
    }

    @Test
    void twoManaAloneCannotPayThreeManaDisguiseCost() {
        addCreatureReady(player1, new TinStreetGossip());
        harness.setHand(player1, List.of(new NervousGardener()));
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void restrictedManaPaysDisguiseAndColoredTurnFaceUpCost() {
        addCreatureReady(player1, new TinStreetGossip());
        addCreatureReady(player1, new TinStreetGossip());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new NervousGardener(), new RedHerring()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent gardener = findPermanent(player1, "Nervous Gardener");
        assertThat(gardener.isFaceDown()).isTrue();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gardener));

        assertThat(gardener.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getFaceDownSpellsOrTurnFaceUpMana(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getFaceDownSpellsOrTurnFaceUpMana(ManaColor.GREEN)).isZero();
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedManaCannotPayUnrelatedActivatedAbility() {
        addCreatureReady(player1, new TinStreetGossip());
        addCreatureReady(player1, new RedHerring());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Red Herring");
    }

    @Test
    void attackingWithVigilanceLeavesGossipUntapped() {
        Permanent gossip = addCreatureReady(player1, new TinStreetGossip());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gossip.isAttacking()).isTrue();
        assertThat(gossip.isTapped()).isFalse();
    }
}
