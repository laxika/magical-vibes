package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesCreatureOrControllerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesCreatureOrCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "2253")
@CardRegistration(set = "2X2", collectorNumber = "293")
@CardRegistration(set = "C17", collectorNumber = "49")
public class WasitoraNekoruQueen extends Card {

    public WasitoraNekoruQueen() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new TargetPlayerSacrificesCreatureOrCreatesTokenEffect(
                        new CreateTokenEffect(1, "Cat Dragon", 3, 3, CardColor.BLACK,
                                Set.of(CardColor.BLACK, CardColor.RED, CardColor.GREEN),
                                List.of(CardSubtype.CAT, CardSubtype.DRAGON),
                                Set.of(Keyword.FLYING), Set.of())));
    }
}
