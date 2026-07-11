package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToOpponentsThisTurn;
import com.github.laxika.magicalvibes.model.condition.CastForProwlCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "45")
public class NotoriousThrong extends Card {

    public NotoriousThrong() {
        // Prowl {5}{U}: cast for this cost if you dealt combat damage to a player this turn with a Rogue.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{5}{U}")), CardSubtype.ROGUE));

        // Create X 1/1 black Faerie Rogue creature tokens with flying, where X is the damage dealt to
        // your opponents this turn.
        addEffect(EffectSlot.SPELL,
                new CreateTokenEffect(new DamageDealtToOpponentsThisTurn(), "Faerie Rogue", 1, 1,
                        CardColor.BLACK, List.of(CardSubtype.FAERIE, CardSubtype.ROGUE),
                        Set.of(Keyword.FLYING), Set.of()));

        // If this spell's prowl cost was paid, take an extra turn after this one.
        addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new CastForProwlCost(), new ControllerExtraTurnEffect(1)));
    }
}
