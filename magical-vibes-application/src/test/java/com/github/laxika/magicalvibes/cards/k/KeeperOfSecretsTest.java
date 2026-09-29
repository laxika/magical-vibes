package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeeperOfSecrets.class, GrizzlyBears.class})
class KeeperOfSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the mana value of a spell cast from exile")
    void damagesTargetOpponentForNonHandSpellManaValue() {
        harness.addToBattlefield(player1, new KeeperOfSecrets());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFromExile(player1, spell.getId());

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger for a spell cast from hand")
    void doesNotTriggerForHandCast() {
        harness.addToBattlefield(player1, new KeeperOfSecrets());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only an opponent can be targeted")
    void onlyOpponentCanBeTargeted() {
        harness.addToBattlefield(player1, new KeeperOfSecrets());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, spell.getId());

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).containsExactly(player2.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
