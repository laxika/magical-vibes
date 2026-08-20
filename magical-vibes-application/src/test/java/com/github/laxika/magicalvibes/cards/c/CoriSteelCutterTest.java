package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CoriSteelCutterTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell creates a Monk and offers to attach the Cutter to it")
    void secondSpellCreatesMonkAndOffersAttachment() {
        Permanent cutter = harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        castTwoLightningBolts();

        Permanent monk = monkTokens().getFirst();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(cutter.getAttachedTo()).isEqualTo(monk.getId());
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, monk, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, monk, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The first spell does not create a Monk")
    void firstSpellDoesNotCreateMonk() {
        harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(monkTokens()).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A later noncreature spell gives the Monk its prowess boost")
    void laterNoncreatureSpellTriggersProwess() {
        harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent monk = monkTokens().getFirst();
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Cutter attaches only to the Monk created by its own trigger")
    void multipleCuttersAttachToTheirOwnTokens() {
        Permanent firstCutter = harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        Permanent secondCutter = harness.addToBattlefieldAndReturn(player1, new CoriSteelCutter());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        List<UUID> attachedTokenIds = List.of(firstCutter.getAttachedTo(), secondCutter.getAttachedTo());
        Set<UUID> tokenIds = monkTokens().stream().map(Permanent::getId).collect(Collectors.toSet());
        assertThat(monkTokens()).hasSize(2);
        assertThat(attachedTokenIds).doesNotContainNull();
        assertThat(Set.copyOf(attachedTokenIds)).isEqualTo(tokenIds);
    }

    private void castTwoLightningBolts() {
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private List<Permanent> monkTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.MONK))
                .toList();
    }
}
