package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IzzetGuildmage.class, CounselOfTheSoratami.class, LavaSpike.class, LightningBolt.class})
class IzzetGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a low-mana-value instant with the blue ability")
    void copiesInstantWithBlueAbility() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addToBattlefield(player1, new IzzetGuildmage());

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, 0, null, bolt.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .singleElement()
                .satisfies(copy -> {
                    assertThat(copy.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
                    assertThat(copy.getTargetId()).isEqualTo(player2.getId());
                });
    }

    @Test
    @DisplayName("Copies a low-mana-value sorcery with the red ability")
    void copiesSorceryWithRedAbility() {
        LavaSpike lavaSpike = new LavaSpike();
        harness.setHand(player1, List.of(lavaSpike));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addToBattlefield(player1, new IzzetGuildmage());

        harness.castSorcery(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, 1, null, lavaSpike.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .singleElement()
                .satisfies(copy -> assertThat(copy.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL));
    }

    @Test
    @DisplayName("Does not allow spells with mana value greater than two")
    void rejectsSpellWithManaValueGreaterThanTwo() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addToBattlefield(player1, new IzzetGuildmage());

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
