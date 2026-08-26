package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FlameRift;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyrCarahTheBold.class, FlameRift.class, GrizzlyBears.class, Shock.class})
class SyrCarahTheBoldTest extends BaseCardTest {

    @Test
    @DisplayName("Its activated ability exiles the top card after damaging a player")
    void activatedAbilityTriggersTheExile() {
        Permanent syrCarah = addReadySyrCarah();
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(syrCarah.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A controlled instant dealing damage to a player exiles the top card")
    void instantDamageToPlayerTriggersTheExile() {
        addReadySyrCarah();
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Damage to a creature by an instant does not trigger the exile")
    void instantDamageToCreatureDoesNotTriggerTheExile() {
        addReadySyrCarah();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("A spell damaging two players creates one trigger for each player")
    void spellDamageToTwoPlayersTriggersTwice() {
        addReadySyrCarah();
        Card first = new Shock();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new FlameRift()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, (UUID) null);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(second.getId(), player1.getId());
    }

    private Permanent addReadySyrCarah() {
        Permanent permanent = new Permanent(new SyrCarahTheBold());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
