package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterworkOfIngenuity.class, LeoninScimitar.class, GrizzlyBears.class})
class MasterworkOfIngenuityTest extends BaseCardTest {

    @Test
    @DisplayName("May enter as a copy of an Equipment on the battlefield")
    void copiesEquipmentAndItsAbilities() {
        Permanent scimitar = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MasterworkOfIngenuity masterwork = new MasterworkOfIngenuity();
        castMasterwork(masterwork);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, scimitar.getId());

        Permanent copy = findCopy(masterwork);
        assertThat(copy.getCard().getName()).isEqualTo("Leonin Scimitar");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(copy), null,
                bears.getId());
        harness.passBothPriorities();

        assertThat(copy.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not offer to copy a non-Equipment")
    void doesNotOfferToCopyNonEquipment() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        MasterworkOfIngenuity masterwork = new MasterworkOfIngenuity();
        castMasterwork(masterwork);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findCopy(masterwork).getCard().getName()).isEqualTo("Masterwork of Ingenuity");
    }

    private void castMasterwork(MasterworkOfIngenuity masterwork) {
        harness.setHand(player1, List.of(masterwork));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findCopy(MasterworkOfIngenuity masterwork) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(masterwork.getId()))
                .findFirst()
                .orElseThrow();
    }
}
