package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AuriokChampion;
import com.github.laxika.magicalvibes.cards.a.AvariceTotem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrarkClanIronworks.class, AvariceTotem.class, AuriokChampion.class})
class KrarkClanIronworksTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Krark-Clan Ironworks adds two colorless mana")
    void sacrificingSourceAddsTwoColorlessMana() {
        harness.addToBattlefield(player1, new KrarkClanIronworks());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Krark-Clan Ironworks");
    }

    @Test
    @DisplayName("Can sacrifice another controlled artifact, but not a non-artifact or opponent's artifact")
    void sacrificesAnotherControlledArtifact() {
        Permanent ironworks = harness.addToBattlefieldAndReturn(player1, new KrarkClanIronworks());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new AvariceTotem());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AuriokChampion());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new AvariceTotem());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(ironworks.getId(), ownArtifact.getId())
                .doesNotContain(ownCreature.getId(), opponentArtifact.getId());
        harness.handlePermanentChosen(player1, ownArtifact.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Krark-Clan Ironworks");
        harness.assertOnBattlefield(player1, "Auriok Champion");
        harness.assertOnBattlefield(player2, "Avarice Totem");
        harness.assertInGraveyard(player1, "Avarice Totem");
    }

}
