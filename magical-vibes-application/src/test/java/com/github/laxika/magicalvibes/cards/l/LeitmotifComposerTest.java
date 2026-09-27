package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeitmotifComposer.class, Divination.class, GrizzlyBears.class, TimeWarp.class})
class LeitmotifComposerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player draws a card")
    void combatDamageDrawsCard() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Divination()));
        harness.setLife(player2, 20);
        addReadyComposer(player1);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting an instant or sorcery with mana value 5 or greater creates a copy")
    void highManaValueInstantOrSorceryCreatesCopy() {
        addReadyComposer(player1);
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Casting a spell below mana value 5 does not create a copy")
    void lowManaValueInstantOrSorceryDoesNotCreateCopy() {
        addReadyComposer(player1);
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The activated ability affects every creature named Leitmotif Composer")
    void activatedAbilityAffectsMatchingCreaturesOnly() {
        Permanent source = addReadyComposer(player1);
        Permanent otherComposer = addReadyComposer(player1);
        Permanent opponentComposer = addReadyComposer(player2);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.isCantBeBlocked()).isTrue();
        assertThat(otherComposer.isCantBeBlocked()).isTrue();
        assertThat(opponentComposer.isCantBeBlocked()).isTrue();
        assertThat(bears.isCantBeBlocked()).isFalse();
    }

    private Permanent addReadyComposer(com.github.laxika.magicalvibes.model.Player player) {
        return addReadyCreature(player, new LeitmotifComposer());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
