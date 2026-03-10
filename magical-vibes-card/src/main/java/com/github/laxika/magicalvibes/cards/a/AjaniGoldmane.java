package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateLifeTotalAvatarTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachOwnCreatureEffect;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "1")
public class AjaniGoldmane extends Card {

    public AjaniGoldmane() {
        // +1: You gain 2 life.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new GainLifeEffect(2)),
                "+1: You gain 2 life."
        ));

        // −1: Put a +1/+1 counter on each creature you control.
        //     Those creatures gain vigilance until end of turn.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(
                        new PutPlusOnePlusOneCounterOnEachOwnCreatureEffect(),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES)
                ),
                "\u22121: Put a +1/+1 counter on each creature you control. Those creatures gain vigilance until end of turn."
        ));

        // −6: Create a white Avatar creature token. It has "This creature's power and
        //     toughness are each equal to your life total."
        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateLifeTotalAvatarTokenEffect(
                        "Avatar",
                        CardColor.WHITE,
                        List.of(CardSubtype.AVATAR)
                )),
                "\u22126: Create a white Avatar creature token. It has \"This creature's power and toughness are each equal to your life total.\""
        ));
    }
}
