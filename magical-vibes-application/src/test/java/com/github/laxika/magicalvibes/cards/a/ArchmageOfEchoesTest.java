package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FaerieNoble;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WanderingMage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchmageOfEchoes.class, FaerieNoble.class, WanderingMage.class, GrizzlyBears.class})
class ArchmageOfEchoesTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a Faerie permanent spell as a token")
    void copiesFaeriePermanentSpellAsToken() {
        harness.addToBattlefield(player1, new ArchmageOfEchoes());
        harness.setHand(player1, List.of(new FaerieNoble()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(entry.isCopy()).isTrue();
        });

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Faerie Noble"));
    }

    @Test
    @DisplayName("Copies a Wizard permanent spell as a token")
    void copiesWizardPermanentSpellAsToken() {
        harness.addToBattlefield(player1, new ArchmageOfEchoes());
        harness.setHand(player1, List.of(new WanderingMage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.isCopy()).isTrue());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Wandering Mage"));
    }

    @Test
    @DisplayName("Does not copy a non-Faerie, non-Wizard permanent spell")
    void doesNotCopyOtherPermanentSpell() {
        harness.addToBattlefield(player1, new ArchmageOfEchoes());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
