# Copy-Paste Templates (Card + Test)

Ready-to-use templates for the most common archetypes. Replace `PLACEHOLDER` values. Each template includes the card class and the full test file.

## Quick template selector

| Oracle text pattern | Template |
|--------------------|----------|
| "Creatures you control get +X/+Y until end of turn" | [Non-targeted pump](#non-targeted-pump-instantsorcery) |
| "CARDNAME deals N damage to any target" | [Targeted burn](#targeted-burn-instantsorcery) |
| "Draw N cards" | [Non-targeted draw](#non-targeted-draw-instantsorcery) |
| "Destroy target creature/permanent" | [Targeted destroy](#targeted-destroy-instantsorcery) |
| "When CARDNAME enters the battlefield, [effect]" | [ETB creature](#etb-creature-non-targeted-effect) |
| "Creatures you control get +X/+Y and gain [keyword]" | [Pump all + keyword](#pump-all--keyword-instantsorcery) |
| "Counter target spell" | [Counter spell](#counter-target-spell-instant) |
| "Counter target [type] spell. [bonus]" | [Filtered counter + bonus](#filtered-counterspell--bonus-effect-instant) |
| "When this creature enters, target creature gets +P/+T" | [ETB pump target](#etb-pump-target-creature) |
| "Enchant creature. Enchanted creature [static effect]" | [Aura with static effect](#aura-with-static-effect-enchant-creature) |
| "Enchant creature. {COST}: [effect on enchanted]" | [Aura with own ability](#aura-with-own-activated-ability-enchant-creature) |
| "Enchant creature. Enchanted creature has '{COST}: [effect]'" | [Aura granting ability](#aura-granting-activated-ability-to-enchanted-creature) |

---

### Non-targeted pump instant/sorcery

"Creatures you control get +X/+Y until end of turn."

Reference: `c/Charge.java` (instant, +1/+1), `b/BarTheDoor.java` (instant, +0/+4), `i/InspiredCharge.java` (instant, +2/+1)

Also covers: pump + conditional keyword (fateful hour, morbid, raid, etc.) — see [Pump all + conditional keyword](#pump-all--conditional-keyword-instantsorcery) template below.

**Card:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SET", collectorNumber = "NUM")
public class CARDNAME extends Card {

    public CARDNAME() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(POWER_BOOST, TOUGHNESS_BOOST));
    }
}
```

**Test:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CARDNAMETest extends BaseCardTest {

    @Test
    @DisplayName("Resolving boosts all own creatures +POWER_BOOST/+TOUGHNESS_BOOST")
    void resolvingBoostsAllOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(POWER_BOOST);
                assertThat(p.getToughnessModifier()).isEqualTo(TOUGHNESS_BOOST);
            }
        }
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        for (Permanent p : p2Battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(0);
            }
        }
    }

    @Test
    @DisplayName("Boost resets at cleanup step")
    void boostResetsAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(0);
            }
        }
    }
}
```

**Placeholders:** `LETTER` (package letter), `SET`/`NUM` (card printing), `CARDNAME` (class name), `POWER_BOOST`/`TOUGHNESS_BOOST` (integers), `PRIMARY_COLOR` (ManaColor), `TOTAL_MANA` (CMC integer), `castInstant`/`castSorcery` + `INSTANT_SPELL`/`SORCERY_SPELL`.

---

### Targeted burn instant/sorcery

"CARDNAME deals N damage to any target."

Reference: `s/Shock.java` (instant, 2 dmg), `l/LightningBolt.java` (instant, 3 dmg)

**Card:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SET", collectorNumber = "NUM")
public class CARDNAME extends Card {

    public CARDNAME() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(DAMAGE, false));
    }
}
```

**Test:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CARDNAMETest extends BaseCardTest {

    @Test
    @DisplayName("Deals DAMAGE damage to target creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Adjust assertion based on DAMAGE vs target toughness
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Deals DAMAGE damage to target player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20 - DAMAGE);
    }

    @Test
    @DisplayName("Fizzles when target becomes illegal")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }
}
```

**Placeholders:** `LETTER`, `SET`/`NUM`, `CARDNAME`, `DAMAGE` (integer), `PRIMARY_COLOR`, `TOTAL_MANA`.

---

### Non-targeted draw instant/sorcery

"Draw N cards."

Reference: `c/CounselOfTheSoratami.java` (sorcery, draw 2)

**Card:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SET", collectorNumber = "NUM")
public class CARDNAME extends Card {

    public CARDNAME() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(DRAW_COUNT));
    }
}
```

**Test:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CARDNAMETest extends BaseCardTest {

    @Test
    @DisplayName("Resolving draws DRAW_COUNT cards")
    void resolvingDrawsCards() {
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1 + DRAW_COUNT);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("CARD_DISPLAY_NAME"));
    }
}
```

**Placeholders:** `LETTER`, `SET`/`NUM`, `CARDNAME`, `CARD_DISPLAY_NAME`, `DRAW_COUNT` (integer), `PRIMARY_COLOR`, `TOTAL_MANA`.

---

### Targeted destroy instant/sorcery

"Destroy target creature."

Reference: `t/Terror.java` (instant, nonblack nonartifact), `m/Murder.java` (instant, any creature)

**Card:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SET", collectorNumber = "NUM")
public class CARDNAME extends Card {

    public CARDNAME() {
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(CANT_REGENERATE));
        setTargetFilter(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate()));
    }
}
```

**Test:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CARDNAMETest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature")
    void destroysTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fizzles when target becomes illegal")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }
}
```

**Placeholders:** `LETTER`, `SET`/`NUM`, `CARDNAME`, `CANT_REGENERATE` (boolean), `PRIMARY_COLOR`, `TOTAL_MANA`.

---

### ETB creature (non-targeted effect)

"When CARDNAME enters the battlefield, [effect]."

Reference: `a/AngelOfMercy.java` (ETB gain 3 life), `k/KavuClimber.java` (ETB draw 1)

**Card:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EFFECT_CLASS;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SET", collectorNumber = "NUM")
public class CARDNAME extends Card {

    public CARDNAME() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EFFECT_CLASS(ARGS));
    }
}
```

**Test:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CARDNAMETest extends BaseCardTest {

    @Test
    @DisplayName("ETB effect triggers on resolution")
    void etbEffectTriggers() {
        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        // CUSTOMIZE: Assert effect happened (life gain, draw, etc.)
    }

    @Test
    @DisplayName("Creature enters the battlefield")
    void creatureEntersBattlefield() {
        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "CARD_DISPLAY_NAME");
    }
}
```

**Placeholders:** `LETTER`, `SET`/`NUM`, `CARDNAME`, `CARD_DISPLAY_NAME`, `EFFECT_CLASS`, `ARGS`, `PRIMARY_COLOR`, `TOTAL_MANA`.

---

### Pump all + keyword instant/sorcery

"Creatures you control get +X/+Y and gain [keyword] until end of turn."

Reference: `o/Overrun.java` (+3/+3, trample)

**Card:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SET", collectorNumber = "NUM")
public class CARDNAME extends Card {

    public CARDNAME() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(POWER_BOOST, TOUGHNESS_BOOST));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.KEYWORD, GrantScope.OWN_CREATURES));
    }
}
```

**Test:** Same structure as the pump instant template above, plus:

```java
    @Test
    @DisplayName("Grants KEYWORD to all own creatures")
    void grantsKeyword() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getGrantedKeywords()).contains(Keyword.KEYWORD);
            }
        }
    }
```

---

### Counter target spell instant

"Counter target spell."

Reference: `c/Cancel.java`

**Card:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SET", collectorNumber = "NUM")
public class CARDNAME extends Card {

    public CARDNAME() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
```

**Test:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CARDNAMETest extends BaseCardTest {

    @Test
    @DisplayName("Counters target spell")
    void countersTargetSpell() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);
        harness.castInstant(player1, 0, gd.stack.getFirst().getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.setHand(player1, List.of(new CARDNAME()));
        harness.addMana(player1, ManaColor.PRIMARY_COLOR, TOTAL_MANA);
        harness.castInstant(player1, 0, gd.stack.getFirst().getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "CARD_DISPLAY_NAME");
    }
}
```

**Placeholders:** `LETTER`, `SET`/`NUM`, `CARDNAME`, `CARD_DISPLAY_NAME`, `PRIMARY_COLOR`, `TOTAL_MANA`.

---

### Filtered counterspell + bonus effect instant

"Counter target [type] spell. [bonus effect]."

Reference: `b/BoneToAsh.java` (counter creature + draw), `p/PsychicBarrier.java` (counter creature + life loss)

**Card:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.BONUS_EFFECT_CLASS;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "SET", collectorNumber = "NUM")
public class CARDNAME extends Card {

    public CARDNAME() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.SPELL_TYPE)),
                "Target must be a SPELL_TYPE_LABEL spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect())
          .addEffect(EffectSlot.SPELL, new BONUS_EFFECT_CLASS(BONUS_ARGS));
    }
}
```

**Placeholders:** `SPELL_TYPE` (e.g. `CREATURE_SPELL`), `SPELL_TYPE_LABEL` (e.g. "creature"), `BONUS_EFFECT_CLASS`/`BONUS_ARGS`.

---

### ETB pump target creature

"When this creature enters, target creature gets +P/+T until end of turn."

Reference: `b/BriarpackAlpha.java` (flash, +2/+2), `v/VulshokHeartstoker.java` (+2/+0)

**Card:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SET", collectorNumber = "NUM")
public class CARDNAME extends Card {

    public CARDNAME() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostTargetCreatureEffect(POWER_BOOST, TOUGHNESS_BOOST));
    }
}
```

**Test:** See `BriarpackAlphaTest.java` for the full test pattern (targeted ETB + fizzle + flash tests).

---

### Aura with static effect (enchant creature)

"Enchant creature. Enchanted creature gets +X/+Y." / "Enchanted creature can't attack or block."

Reference: `h/HolyStrength.java` (+1/+2 boost), `p/Pacifism.java` (can't attack/block)

Best test reference: `SensoryDeprivationTest.java` (covers casting, resolution, stat check, removal, fizzle, targeting)

**Card:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.STATIC_EFFECT_CLASS;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SET", collectorNumber = "NUM")
public class CARDNAME extends Card {

    public CARDNAME() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.STATIC, new STATIC_EFFECT_CLASS(ARGS));
    }
}
```

**Test:** Follow `SensoryDeprivationTest.java` pattern — covers:
1. Casting puts on stack
2. Resolving attaches to target
3. Static effect active (check with `gqs.getEffectivePower/Toughness`)
4. Effect stops when aura removed
5. Fizzles if target removed before resolution
6. Cannot target noncreature

**Placeholders:** `STATIC_EFFECT_CLASS` (e.g. `StaticBoostEffect`, `EnchantedCreatureCantAttackOrBlockEffect`), `ARGS` (e.g. `-13, 0, GrantScope.ENCHANTED_CREATURE`).

---

### Aura with own activated ability (enchant creature)

"Enchant creature. {COST}: [effect on enchanted creature]."

Reference: `b/BurdenOfGuilt.java` ({1}: Tap enchanted creature)

**Card:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EFFECT_CLASS;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SET", collectorNumber = "NUM")
public class CARDNAME extends Card {

    public CARDNAME() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{MANA_COST}",
                List.of(new EFFECT_CLASS(ARGS)),
                "{MANA_COST}: ABILITY_DESCRIPTION."
        ));
    }
}
```

**Key detail:** `activateAbility` index points to the aura's position on the battlefield, NOT the enchanted creature.

---

### Aura granting activated ability to enchanted creature

"Enchant creature. Enchanted creature has '{COST}: [effect].'"

Reference: `a/ArcaneTeachings.java` (+2/+2, {T}: deal 1 damage)

**Card:**

```java
package com.github.laxika.magicalvibes.cards.LETTER;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GRANTED_EFFECT_CLASS;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "SET", collectorNumber = "NUM")
public class CARDNAME extends Card {

    public CARDNAME() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        REQUIRES_TAP,
                        "ABILITY_MANA_COST_OR_NULL",
                        List.of(new GRANTED_EFFECT_CLASS(ARGS)),
                        "ABILITY_RULES_TEXT"
                ),
                GrantScope.ENCHANTED_CREATURE
        ));
    }
}
```

**Key detail:** `activateAbility` index points to the **creature** (which now has the granted ability), not the aura.
