package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrzasWorkshop.class, UrzasMine.class, UrzasTower.class, UrzasBauble.class, Spellbook.class, LeoninScimitar.class})
class UrzasWorkshopTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping alone adds one colorless mana")
    void tapAloneAddsOne() {
        harness.addToBattlefield(player1, new UrzasWorkshop());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Metalcraft ability cannot be activated without three artifacts")
    void metalcraftRequiresThreeArtifacts() {
        harness.addToBattlefield(player1, new UrzasWorkshop());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Metalcraft");
    }

    @Test
    @DisplayName("Metalcraft ability adds one colorless mana for each Urza's land controlled")
    void metalcraftAddsManaForEachUrzasLand() {
        harness.addToBattlefield(player1, new UrzasWorkshop());
        harness.addToBattlefield(player1, new UrzasMine());
        harness.addToBattlefield(player1, new UrzasTower());
        harness.addToBattlefield(player1, new UrzasBauble());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }
}
