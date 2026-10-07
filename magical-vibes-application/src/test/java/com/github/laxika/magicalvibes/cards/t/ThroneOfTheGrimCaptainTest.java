package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DaringBuccaneer;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.m.MerfolkLooter;
import com.github.laxika.magicalvibes.cards.o.OakenSiren;
import com.github.laxika.magicalvibes.cards.q.QuintoriusKand;
import com.github.laxika.magicalvibes.cards.r.RiverHeraldScout;
import com.github.laxika.magicalvibes.cards.t.ThrashingBrontodon;
import com.github.laxika.magicalvibes.cards.v.VampireNoble;
import com.github.laxika.magicalvibes.cards.v.VitosInquisitor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThroneOfTheGrimCaptain.class, TheGrimCaptain.class, ThrashingBrontodon.class,
        MerfolkLooter.class, DaringBuccaneer.class, VampireNoble.class, DarksteelRelic.class,
        QuintoriusKand.class, OakenSiren.class, RiverHeraldScout.class, VitosInquisitor.class})
class ThroneOfTheGrimCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the Throne mills two cards")
    void millsTwoCards() {
        harness.addToBattlefieldAndReturn(player1, new ThroneOfTheGrimCaptain());
        Card first = new VampireNoble();
        Card second = new MerfolkLooter();
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Crafting requires one material of each required creature type")
    void craftsWithRequiredCreatureTypes() {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfTheGrimCaptain());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new ThrashingBrontodon());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkLooter());
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new DaringBuccaneer());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireNoble());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThat(gs.canActivateAbility(gd, player1.getId(), throne, 1,
                gd.playerManaPools.get(player1.getId()))).isTrue();

        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(throne);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof TheGrimCaptain);
        assertThat(gd.findExiledCard(dinosaur.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(merfolk.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(pirate.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(vampire.getCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Crafting fails when the available materials do not cover all required types")
    void rejectsMissingRequiredCreatureType() {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfTheGrimCaptain());
        harness.addToBattlefieldAndReturn(player1, new ThrashingBrontodon());
        harness.addToBattlefieldAndReturn(player1, new ThrashingBrontodon());
        harness.addToBattlefieldAndReturn(player1, new ThrashingBrontodon());
        harness.addToBattlefieldAndReturn(player1, new ThrashingBrontodon());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThat(gs.canActivateAbility(gd, player1.getId(), throne, 1,
                gd.playerManaPools.get(player1.getId()))).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one of each required creature type");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(throne);
    }

    @Test
    @DisplayName("Attacking forces an opponent to sacrifice and may return an exiled creature tapped and attacking")
    void attackSacrificesAndReturnsCraftMaterial() {
        Permanent captain = addReadyCaptain();
        Card returned = new MerfolkLooter();
        gd.addToExile(player1.getId(), returned, captain.getId());
        gd.addToExile(player1.getId(), new DarksteelRelic(), captain.getId());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);
        Permanent returnedPermanent = findPermanent(player1, returned.getName());
        assertThat(returnedPermanent.isTapped()).isTrue();
        harness.assertLife(player2, 12);
    }

    @Test
    void millsOnlyRemainingCardWhenLibraryHasOneCard() {
        harness.addToBattlefield(player1, new ThroneOfTheGrimCaptain());
        Card remaining = new ThrashingBrontodon();
        harness.setLibrary(player1, List.of(remaining));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void craftsWithMixedBattlefieldAndGraveyardMaterials() {
        harness.addToBattlefield(player1, new ThroneOfTheGrimCaptain());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new ThrashingBrontodon());
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new OakenSiren());
        Card merfolk = new RiverHeraldScout();
        Card vampire = new VitosInquisitor();
        harness.setGraveyard(player1, List.of(merfolk, vampire));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dinosaur, pirate);
        Permanent captain = findPermanent(player1, "The Grim Captain");
        assertThat(captain.isTransformed()).isTrue();
        for (Card material : List.of(dinosaur.getCard(), pirate.getCard(), merfolk, vampire)) {
            assertThat(gd.findExiledCard(material.getId()).sourcePermanentId()).isEqualTo(captain.getId());
        }
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void craftCannotBeActivatedDuringCombat() {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfTheGrimCaptain());
        harness.setGraveyard(player1, List.of(new ThrashingBrontodon(), new RiverHeraldScout(),
                new OakenSiren(), new VitosInquisitor()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gs.canActivateAbility(gd, player1.getId(), throne, 1,
                gd.playerManaPools.get(player1.getId()))).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    void mayDeclineReturningMaterialWithoutPreventingSacrifice() {
        Permanent captain = addReadyCaptain();
        Card material = new ThrashingBrontodon();
        gd.addToExile(player1.getId(), material, captain.getId());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new ThroneOfTheGrimCaptain());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, material.getName());
    }

    @Test
    void returnedCreatureAllowsChoosingBetweenPlayerAndPlaneswalker() {
        Permanent captain = addReadyCaptain();
        gd.addToExile(player1.getId(), new ThrashingBrontodon(), captain.getId());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new ThroneOfTheGrimCaptain());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new QuintoriusKand());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
            harness.handleMultiplePermanentsChosen(player2, List.of(victim.getId()));
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker);
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
        });
    }

    private Permanent addReadyCaptain() {
        ThroneOfTheGrimCaptain front = new ThroneOfTheGrimCaptain();
        Permanent captain = harness.addToBattlefieldAndReturn(player1, front);
        captain.setCard(front.getBackFaceCard());
        captain.setTransformed(true);
        captain.setSummoningSick(false);
        return captain;
    }
}
