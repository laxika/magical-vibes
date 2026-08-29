package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrotesqueHybrid.class, GiantSpider.class, Shock.class})
class GrotesqueHybridTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage destroys the damaged creature despite a regeneration shield")
    void combatDamageDestroysWithoutRegeneration() {
        addReadyPermanent(player1, new GrotesqueHybrid());
        Permanent spider = addReadyPermanent(player2, new GiantSpider());
        spider.setRegenerationShield(1);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(spider);
    }

    @Test
    @DisplayName("The trigger does not fire for noncombat damage")
    void noncombatDamageDoesNotDestroy() {
        addReadyPermanent(player1, new GrotesqueHybrid());
        Permanent spider = addReadyPermanent(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, spider.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spider);
    }

    @Test
    @DisplayName("Discarding a card grants flying and protection from green and white until end of turn")
    void discardGrantsFlyingAndProtection() {
        Permanent hybrid = addReadyPermanent(player1, new GrotesqueHybrid());
        Card discarded = new Shock();
        harness.setHand(player1, List.of(discarded));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(hybrid.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(hybrid.getProtectionFromColorsUntilEndOfTurn())
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);

        hybrid.resetModifiers();
        assertThat(hybrid.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(hybrid.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
