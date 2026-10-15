package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.i.IsochronScepter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErrantStreetArtist.class, CounselOfTheSoratami.class, Twincast.class,
        Shock.class, IsochronScepter.class})
class ErrantStreetArtistTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a spell you control that wasn't cast")
    void copiesSpellThatWasNotCast() {
        addReadyErrant(player2);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, counsel.getId());

        UUID copyId = gd.stack.stream()
                .filter(StackEntry::isCopy)
                .map(entry -> entry.getCard().getId())
                .findFirst()
                .orElseThrow();
        harness.passPriority(player1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateAbility(player2, 0, 0, null, copyId);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .allMatch(copy -> copy.getControllerId().equals(player2.getId()));
    }

    @Test
    @DisplayName("Cannot target a spell that was cast")
    void cannotTargetCastSpell() {
        addReadyErrant(player1);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsUncastCopy() {
        addReadyErrant(player1);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, counsel.getId());
        UUID copyId = gd.stack.getLast().getTargetableId();

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, copyId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseNewTargetWithoutChangingOriginalCopy() {
        UUID originalCopyId = createShockCopy();
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, 0, null, originalCopyId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getTargetableId().equals(originalCopyId))
                .hasSize(1)
                .allMatch(entry -> entry.getTargetId().equals(player2.getId()));
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void canKeepOriginalTargetsAndPaysManaAndTapCosts() {
        UUID copyId = createShockCopy();
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, 0, null, copyId);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotTargetCopyThatWasCastByIsochronScepter() {
        addReadyErrant(player1);
        IsochronScepter scepter = new IsochronScepter();
        Shock shock = new Shock();
        gd.setImprintedCard(scepter, shock);
        gd.exiledCards.add(new ExiledCardEntry(shock, player1.getId(), scepter.getId()));
        harness.addToBattlefield(player1, scepter);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        UUID castCopyId = gd.stack.getLast().getTargetableId();

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, castCopyId))
                .isInstanceOf(IllegalStateException.class);
    }

    private UUID createShockCopy() {
        harness.addToBattlefield(player2, new ErrantStreetArtist());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, shock.getId());
        harness.handleMayAbilityChosen(player2, false);
        return gd.stack.getLast().getTargetableId();
    }

    private void addReadyErrant(Player player) {
        Permanent errant = harness.addToBattlefieldAndReturn(player, new ErrantStreetArtist());
        errant.setSummoningSick(false);
    }
}
