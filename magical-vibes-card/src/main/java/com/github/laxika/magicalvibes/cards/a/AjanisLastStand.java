package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "4")
public class AjanisLastStand extends Card {

    public AjanisLastStand() {
        // Whenever a creature or planeswalker you control dies, you may sacrifice this
        // enchantment. If you do, create a 4/4 white Avatar creature token with flying.
        addEffect(EffectSlot.ON_ALLY_CREATURE_OR_PLANESWALKER_DIES,
                new MayEffect(new SacrificeSelfThenEffect(avatarToken()),
                        "Sacrifice Ajani's Last Stand to create a 4/4 white Avatar with flying?"));

        // When a spell or ability an opponent controls causes you to discard this card, if you
        // control a Plains, create a 4/4 white Avatar creature token with flying.
        addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT,
                new ConditionalEffect(
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS)),
                        avatarToken()));
    }

    private static CreateTokenEffect avatarToken() {
        return new CreateTokenEffect("Avatar", 4, 4, CardColor.WHITE,
                List.of(CardSubtype.AVATAR), Set.of(Keyword.FLYING), Set.of());
    }
}
