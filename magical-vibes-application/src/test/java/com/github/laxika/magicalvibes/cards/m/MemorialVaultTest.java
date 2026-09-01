package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemorialVault.class, SolRing.class, Forest.class, GrizzlyBears.class})
class MemorialVaultTest extends BaseCardTest {

    @Test
    void exilesOnePlusSacrificedArtifactsManaValueAndGrantsPlayPermission() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new MemorialVault());
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificedArtifact.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .doesNotContainKey(third.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(first.getId(), second.getId());
    }

    @Test
    void cannotSacrificeMemorialVaultItself() {
        harness.addToBattlefieldAndReturn(player1, new MemorialVault());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
