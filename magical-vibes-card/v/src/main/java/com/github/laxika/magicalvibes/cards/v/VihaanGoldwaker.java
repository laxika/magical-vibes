package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "8")
@CardRegistration(set = "OTC", collectorNumber = "44")
public class VihaanGoldwaker extends Card {

    private static final Set<CardSubtype> OUTLAW_SUBTYPES = Set.of(
            CardSubtype.ASSASSIN,
            CardSubtype.MERCENARY,
            CardSubtype.PIRATE,
            CardSubtype.ROGUE,
            CardSubtype.WARLOCK);

    public VihaanGoldwaker() {
        // Other outlaws you control have vigilance and haste.
        addStaticEffect(new StaticBoostEffect(0, 0,
                Set.of(Keyword.VIGILANCE, Keyword.HASTE), GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(OUTLAW_SUBTYPES)));

        // At the beginning of combat on your turn, you may have Treasures you control become
        // 3/3 Construct Assassin artifact creatures in addition to their other types until end of turn.
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new MayEffect(
                new AnimatePermanentsEffect(
                        new Fixed(3), new Fixed(3),
                        List.of(CardSubtype.CONSTRUCT, CardSubtype.ASSASSIN),
                        Set.of(),
                        null,
                        Set.of(CardType.ARTIFACT),
                        GrantScope.OWN_PERMANENTS,
                        EffectDuration.UNTIL_END_OF_TURN,
                        new PermanentHasSubtypePredicate(CardSubtype.TREASURE)),
                "Have Treasures you control become 3/3 Construct Assassin artifact creatures?"));
    }
}
